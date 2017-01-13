package jazmin.server.msg.kcp;
import java.util.ArrayList;

public abstract class KCP {
    public final int IKCP_RTO_NDL = 30;  // no delay min rto
    public final int IKCP_RTO_MIN = 100; // normal min rto
    public final int IKCP_RTO_DEF = 200;
    public final int IKCP_RTO_MAX = 60000;
    public final int IKCP_CMD_PUSH = 81; // cmd: push data
    public final int IKCP_CMD_ACK = 82; // cmd: ack
    public final int IKCP_CMD_WASK = 83; // cmd: window probe (ask)
    public final int IKCP_CMD_WINS = 84; // cmd: window size (tell)
    public final int IKCP_ASK_SEND = 1;  // need to send IKCP_CMD_WASK
    public final int IKCP_ASK_TELL = 2;  // need to send IKCP_CMD_WINS
    public final int IKCP_WND_SND = 32;
    public final int IKCP_WND_RCV = 32;
    public final int IKCP_MTU_DEF = 576;
    public final int IKCP_ACK_FAST = 3;
    public final int IKCP_INTERVAL = 100;
    public final int IKCP_OVERHEAD = 24;
    public final int IKCP_DEADLINK = 10;
    public final int IKCP_THRESH_INIT = 2;
    public final int IKCP_THRESH_MIN = 2;
    public final int IKCP_PROBE_INIT = 7000;   // 7 secs to probe window size
    public final int IKCP_PROBE_LIMIT = 120000; // up to 120 secs to probe window

    protected abstract void output(byte[] buffer, int size); // 需具体实现

    // encode 8 bits unsigned int
    public static void ikcpEncode8u(byte[] p, int offset, byte c) {
        p[0 + offset] = c;
    }

    // decode 8 bits unsigned int
    public static byte ikcpDecode8u(byte[] p, int offset) {
        return p[0 + offset];
    }

    /* encode 16 bits unsigned int (msb) */
    public static void ikcpEncode16u(byte[] p, int offset, int w) {
        p[offset + 0] = (byte) (w >> 8);
        p[offset + 1] = (byte) (w >> 0);
    }

    /* decode 16 bits unsigned int (msb) */
    public static int ikcpDecode16u(byte[] p, int offset) {
        int ret = (p[offset + 0] & 0xFF) << 8
                | (p[offset + 1] & 0xFF);
        return ret;
    }

    /* encode 32 bits unsigned int (msb) */
    public static void ikcpEncode32u(byte[] p, int offset, int l) {
        p[offset + 0] = (byte) (l >> 24);
        p[offset + 1] = (byte) (l >> 16);
        p[offset + 2] = (byte) (l >> 8);
        p[offset + 3] = (byte) (l >> 0);
    }

    /* decode 32 bits unsigned int (msb) */
    public static int ikcpDecode32u(byte[] p, int offset) {
        int ret = (p[offset + 0] & 0xFF) << 24
                | (p[offset + 1] & 0xFF) << 16
                | (p[offset + 2] & 0xFF) << 8
                | p[offset + 3] & 0xFF;
        return ret;
    }

    public static void slice(ArrayList<Segment> list, int start, int stop) {
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            if (i < stop - start) {
                list.set(i, list.get(i + start));
            } else {
                list.remove(stop - start);
            }
        }
    }

    static int imin(int a, int b) {
        return a <= b ? a : b;
    }

    static int imax(int a, int b) {
        return a >= b ? a : b;
    }

    static int ibound(int lower, int middle, int upper) {
        return imin(imax(lower, middle), upper);
    }

    static int itimediff(long later, long earlier) {
        return ((int) (later - earlier));
    }

    private static class Segment {

        protected int conv = 0;
        protected int cmd = 0;
        protected int frg = 0;
        protected int wnd = 0;
        protected long ts = 0;
        protected int sn = 0;
        protected int una = 0;
        protected long resendts = 0;
        protected int rto = 0;
        protected int fastack = 0;
        protected int xmit = 0;
        protected byte[] data;

        protected Segment(int size) {
            this.data = new byte[size];
        }

        // encode a segment into buffer
        protected int encode(byte[] ptr, int offset) {
            int offset_ = offset;

            ikcpEncode32u(ptr, offset, conv);
            offset += 4;
            ikcpEncode8u(ptr, offset, (byte) cmd);
            offset += 1;
            ikcpEncode8u(ptr, offset, (byte) frg);
            offset += 1;
            ikcpEncode16u(ptr, offset, (int) wnd);
            offset += 2;
            ikcpEncode32u(ptr, offset, (int)ts);
            offset += 4;
            ikcpEncode32u(ptr, offset, sn);
            offset += 4;
            ikcpEncode32u(ptr, offset, una);
            offset += 4;
            ikcpEncode32u(ptr, offset, data.length);
            offset += 4;

            return offset - offset_;
        }
    }

    protected int conv = 0;
    //int user = user;
    int sndUna = 0;
    int snd_nxt = 0;
    int rcvnxt = 0;
    long ts_recent = 0;
    long ts_lastack = 0;
    long ts_probe = 0;
    int probe_wait = 0;
    int snd_wnd = IKCP_WND_SND;
    int rcvWnd = IKCP_WND_RCV;
    int rmt_wnd = IKCP_WND_RCV;
    int cwnd = 0;
    int incr = 0;
    int probe = 0;
    protected int mtu = IKCP_MTU_DEF;
    int mss = this.mtu - IKCP_OVERHEAD;
    byte[] buffer = new byte[(int) (mtu + IKCP_OVERHEAD) * 3];
    ArrayList<Segment> nrcvbuf = new ArrayList<>(128);
    ArrayList<Segment> nsndBuf = new ArrayList<>(128);
    ArrayList<Segment> nrcvque = new ArrayList<>(128);
    ArrayList<Segment> nsndque = new ArrayList<>(128);
    int state = 0;
    ArrayList<Integer> acklist = new ArrayList<>(128);
    //int ackblock = 0;
    //int ackcount = 0;
    int rxsrtt = 0;
    int rxrttval = 0;
    int rxrto = IKCP_RTO_DEF;
    int rxminrto = IKCP_RTO_MIN;
    long current = 0;
    long interval = IKCP_INTERVAL;
    long tsflush = IKCP_INTERVAL;
    int nodelay = 0;
    int updated = 0;
    int logmask = 0;
    int ssthresh = IKCP_THRESH_INIT;
    int fastresend = 0;
    int nocwnd = 0;
    int xmit = 0;
    int deadlink = IKCP_DEADLINK;
    //
    public KCP(int conv) {
        this.conv = conv;
    }

    // check the size of next message in the recv queue
    // 计算接收队列中有多少可用的数据
    public int peekSize() {
        if (0 == nrcvque.size()) {
            return -1;
        }

        Segment seq = nrcvque.get(0);

        if (0 == seq.frg) {
            return seq.data.length;
        }

        if (nrcvque.size() < seq.frg + 1) {
            return -1;
        }

        int length = 0;

        for (Segment item : nrcvque) {
            length += item.data.length;
            if (0 == item.frg) {
                break;
            }
        }

        return length;
    }

    // user/upper level recv: returns size, returns below zero for EAGAIN
    // 将接收队列中的数据传递给上层引用
    public int recv(byte[] buffer) {

        if (0 == nrcvque.size()) {
            return -1;
        }

        int peekSize = peekSize();
        if (0 > peekSize) {
            return -2;
        }

        if (peekSize > buffer.length) {
            return -3;
        }

        boolean fastrecover = false;
        if (nrcvque.size() >= rcvWnd) {
            fastrecover = true;
        }

        // merge fragment.
        int count = 0;
        int n = 0;
        for (Segment seg : nrcvque) {
            System.arraycopy(seg.data, 0, buffer, n, seg.data.length);
            n += seg.data.length;
            count++;
            if (0 == seg.frg) {
                break;
            }
        }

        if (0 < count) {
            slice(nrcvque, count, nrcvque.size());
        }

        // move available data from rcv_buf -> nrcv_que
        count = 0;
        for (Segment seg : nrcvbuf) {
            if (seg.sn == rcvnxt && nrcvque.size() < rcvWnd) {
                nrcvque.add(seg);
                rcvnxt++;
                count++;
            } else {
                break;
            }
        }

        if (0 < count) {
            slice(nrcvbuf, count, nrcvbuf.size());
        }

        // fast recover
        if (nrcvque.size() < rcvWnd && fastrecover) {
            // ready to send back IKCP_CMD_WINS in ikcp_flush
            // tell remote my window size
            probe |= IKCP_ASK_TELL;
        }

        return n;
    }

    // user/upper level send, returns below zero for error
    // 上层要发送的数据丢给发送队列，发送队列会根据mtu大小分片
    public int send(byte[] buffer) {
        if (0 == buffer.length) {
            return -1;
        }

        int count;
        if (buffer.length < mss) {
            count = 1;
        } else {
            count = (int) (buffer.length + mss - 1) / mss;
        }

        if (count > 255 ) {
            //return -2;
            throw new IllegalArgumentException("buffer is two long >255 mss");
        }

        if (count == 0 ) {
            count = 1;
        }

        int offset = 0;
        int bufferLength=buffer.length;
        for (int i = 0; i < count; i++) {
            int size = (int) (bufferLength > mss ? mss : bufferLength);
            Segment seg = new Segment(size);
            System.arraycopy(buffer, offset, seg.data, 0, size);
            offset += size;
            seg.frg = count - i - 1;
            nsndque.add(seg);
            bufferLength-=size;
        }
        return 0;
    }

    // update ack.
    void updateAck(int rtt) {
        if (0 == rxsrtt) {
            rxsrtt = rtt;
            rxrttval = rtt / 2;
        } else {
            int delta = (int) (rtt - rxsrtt);
            if (0 > delta) {
                delta = -delta;
            }

            rxrttval = (3 * rxrttval + delta) / 4;
            rxsrtt = (7 * rxsrtt + rtt) / 8;
            if (rxsrtt < 1) {
                rxsrtt = 1;
            }
        }

        int rto = (int) (rxsrtt + imax(1, 4 * rxrttval));
        rxrto = ibound(rxminrto, rto, IKCP_RTO_MAX);
    }

    // 计算本地真实snd_una
    void shrinkBuf() {
        if (nsndBuf.size() > 0) {
            sndUna = nsndBuf.get(0).sn;
        } else {
            sndUna = snd_nxt;
        }
    }

    // 对端返回的ack, 确认发送成功时，对应包从发送缓存中移除
    void parseAck(int sn) {
        if (itimediff(sn, sndUna) < 0 || itimediff(sn, snd_nxt) >= 0) {
            return;
        }

        int index = 0;
        for (Segment seg : nsndBuf) {
            if (sn == seg.sn) {
                nsndBuf.remove(index);
                break;
            } else {
                seg.fastack++;
            }
            index++;
        }
    }

    // 通过对端传回的una将已经确认发送成功包从发送缓存中移除
    void parseUna(int una) {
        int count = 0;
        for (Segment seg : nsndBuf) {
            if (itimediff(una, seg.sn) > 0) {
                count++;
            } else {
                break;
            }
        }

        if (0 < count) {
            slice(nsndBuf, count, nsndBuf.size());
        }
    }

    void ackPush(int sn, int ts) {
        // c原版实现中按*2扩大容量,java不用
        acklist.add(sn);
        acklist.add(ts);
    }

    // 用户数据包解析
    void parseData(Segment newseg) {
        int sn = newseg.sn;
        boolean repeat = false;

        if (itimediff(sn, rcvnxt + rcvWnd) >= 0 || itimediff(sn, rcvnxt) < 0) {
            return;
        }

        int n = nrcvbuf.size() - 1;
        int afteridx = -1;

        // 判断是否是重复包，并且计算插入位置
        for (int i = n; i >= 0; i--) {
            Segment seg = nrcvbuf.get(i);
            if (seg.sn == sn) {
                repeat = true;
                break;
            }

            if (itimediff(sn, seg.sn) > 0) {
                afteridx = i;
                break;
            }
        }

        // 如果不是重复包，则插入
        if (!repeat) {
            if (afteridx == -1) {
                nrcvbuf.add(0, newseg);
            } else {
                nrcvbuf.add(afteridx + 1, newseg);
            }
        }

        // move available data from nrcv_buf -> nrcv_que
        // 将连续包加入到接收队列
        int count = 0;
        for (Segment seg : nrcvbuf) {
            if (seg.sn == rcvnxt && nrcvque.size() < rcvWnd) {
                nrcvque.add(seg);
                rcvnxt++;
                count++;
            } else {
                break;
            }
        }

        // 从接收缓存中移除
        if (0 < count) {
            slice(nrcvbuf, count, nrcvbuf.size());
        }
    }
    //
    public static int getConversionId(byte[] data){
    	if(data.length<4){
    		return -1;
    	}
    	return ikcpDecode32u(data, 0);
    }
    // when you received a low level packet (eg. UDP packet), call it
    // 底层收包后调用，再由上层通过Recv获得处理后的数据
    public int input(byte[] data) {

        int s_una = sndUna;
        if (data.length < IKCP_OVERHEAD) {
            return 0;
        }

        int offset = 0;

        while (true) {
        	int ts, sn, length, una, conv_;
            int wnd;
            byte cmd, frg;

            if (data.length - offset < IKCP_OVERHEAD) {
                break;
            }

            conv_ = ikcpDecode32u(data, offset);
            offset += 4;

            if (conv != conv_) {
                return -1;
            }

            cmd = ikcpDecode8u(data, offset);
            offset += 1;
            frg = ikcpDecode8u(data, offset);
            offset += 1;
            wnd = ikcpDecode16u(data, offset);
            offset += 2;
            ts = ikcpDecode32u(data, offset);
            offset += 4;
            sn = ikcpDecode32u(data, offset);
            offset += 4;
            una = ikcpDecode32u(data, offset);
            offset += 4;
            length = ikcpDecode32u(data, offset);
            offset += 4;
            if (data.length - offset < length) {
                return -2;
            }

            if (cmd != IKCP_CMD_PUSH && cmd != IKCP_CMD_ACK && cmd != IKCP_CMD_WASK && cmd != IKCP_CMD_WINS) {
                return -3;
            }

            rmt_wnd =  wnd;
            parseUna(una);
            shrinkBuf();

            if (IKCP_CMD_ACK == cmd) {
                if (itimediff(current, ts) >= 0) {
                    updateAck(itimediff(current, ts));
                }
                parseAck(sn);
                shrinkBuf();
            } else if (IKCP_CMD_PUSH == cmd) {
                if (itimediff(sn, rcvnxt + rcvWnd) < 0) {
                    ackPush(sn, ts);
                    if (itimediff(sn, rcvnxt) >= 0) {
                        Segment seg = new Segment((int) length);
                        seg.conv = conv_;
                        seg.cmd = cmd;
                        seg.frg = frg;
                        seg.wnd = wnd;
                        seg.ts = ts;
                        seg.sn = sn;
                        seg.una = una;

                        if (length > 0) {
                            System.arraycopy(data, offset, seg.data, 0, (int) length);
                        }

                        parseData(seg);
                    }
                }
            } else if (IKCP_CMD_WASK == cmd) {
                // ready to send back IKCP_CMD_WINS in Ikcp_flush
                // tell remote my window size
                probe |= IKCP_ASK_TELL;
            } else if (IKCP_CMD_WINS == cmd) {
                // do nothing
            } else {
                return -3;
            }

            offset += (int) length;
        }

        if (itimediff(sndUna, s_una) > 0) {
            if (cwnd < rmt_wnd) {
                int mss_ = mss;
                if (cwnd < ssthresh) {
                    cwnd++;
                    incr += mss_;
                } else {
                    if (incr < mss_) {
                        incr = mss_;
                    }
                    incr += (mss_ * mss_) / incr + (mss_ / 16);
                    if ((cwnd + 1) * mss_ <= incr) {
                        cwnd++;
                    }
                }
                if (cwnd > rmt_wnd) {
                    cwnd = rmt_wnd;
                    incr = rmt_wnd * mss_;
                }
            }
        }

        return 0;
    }

    // 接收窗口可用大小
    int wnd_unused() {
        if (nrcvque.size() < rcvWnd) {
            return (int) (int) rcvWnd - nrcvque.size();
        }
        return 0;
    }

    // flush pending data
    void flush() {
    	long current_ = current;
        int change = 0;
        int lost = 0;

        if (0 == updated) {
            return;
        }

        Segment seg = new Segment(0);
        seg.conv = conv;
        seg.cmd = IKCP_CMD_ACK;
        seg.wnd =  wnd_unused();
        seg.una = rcvnxt;

        // flush acknowledges
        int count = acklist.size() / 2;
        int offset = 0;
        for (int i = 0; i < count; i++) {
            if (offset + IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }
            // ikcp_ack_get
            seg.sn = acklist.get(i * 2 + 0);
            seg.ts = acklist.get(i * 2 + 1);
            offset += seg.encode(buffer, offset);
        }
        acklist.clear();

        // probe window size (if remote window size equals zero)
        if (0 == rmt_wnd) {
            if (0 == probe_wait) {
                probe_wait = IKCP_PROBE_INIT;
                ts_probe = current + probe_wait;
            } else {
                if (itimediff(current, ts_probe) >= 0) {
                    if (probe_wait < IKCP_PROBE_INIT) {
                        probe_wait = IKCP_PROBE_INIT;
                    }
                    probe_wait += probe_wait / 2;
                    if (probe_wait > IKCP_PROBE_LIMIT) {
                        probe_wait = IKCP_PROBE_LIMIT;
                    }
                    ts_probe = current + probe_wait;
                    probe |= IKCP_ASK_SEND;
                }
            }
        } else {
            ts_probe = 0;
            probe_wait = 0;
        }

        // flush window probing commands
        if ((probe & IKCP_ASK_SEND) != 0) {
            seg.cmd = IKCP_CMD_WASK;
            if (offset + IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }
            offset += seg.encode(buffer, offset);
        }

        // flush window probing commands(c#)
        if ((probe & IKCP_ASK_TELL) != 0) {
            seg.cmd = IKCP_CMD_WINS;
            if (offset + IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }
            offset += seg.encode(buffer, offset);
        }

        probe = 0;

        // calculate window size
        int cwnd_ = imin(snd_wnd, rmt_wnd);
        if (0 == nocwnd) {
            cwnd_ = imin(cwnd, cwnd_);
        }

        count = 0;
        for (Segment nsnd_que1 : nsndque) {
            if (itimediff(snd_nxt, sndUna + cwnd_) >= 0) {
                break;
            }
            Segment newseg = nsnd_que1;
            newseg.conv = conv;
            newseg.cmd = IKCP_CMD_PUSH;
            newseg.wnd = seg.wnd;
            newseg.ts = current_;
            newseg.sn = snd_nxt;
            newseg.una = rcvnxt;
            newseg.resendts = current_;
            newseg.rto = rxrto;
            newseg.fastack = 0;
            newseg.xmit = 0;
            nsndBuf.add(newseg);
            snd_nxt++;
            count++;
        }

        if (0 < count) {
            slice(nsndque, count, nsndque.size());
        }

        // calculate resent
        int resent = (fastresend > 0) ? fastresend : 0xffffffff;
        int rtomin = (nodelay == 0) ? (rxrto >> 3) : 0;

        // flush data segments
        for (Segment segment : nsndBuf) {
            boolean needsend = false;
            if (0 == segment.xmit) {
                needsend = true;
                segment.xmit++;
                segment.rto = rxrto;
                segment.resendts = current_ + segment.rto + rtomin;
            } else if (itimediff(current_, segment.resendts) >= 0) {
                needsend = true;
                segment.xmit++;
                xmit++;
                if (0 == nodelay) {
                    segment.rto += rxrto;
                } else {
                    segment.rto += rxrto / 2;
                }
                segment.resendts = current_ + segment.rto;
                lost = 1;
            } else if (segment.fastack >= resent) {
                needsend = true;
                segment.xmit++;
                segment.fastack = 0;
                segment.resendts = current_ + segment.rto;
                change++;
            }

            if (needsend) {
                segment.ts = current_;
                segment.wnd = seg.wnd;
                segment.una = rcvnxt;

                int need = IKCP_OVERHEAD + segment.data.length;
                if (offset + need >= mtu) {
                    output(buffer, offset);
                    offset = 0;
                }

                offset += segment.encode(buffer, offset);
                if (segment.data.length > 0) {
                    System.arraycopy(segment.data, 0, buffer, offset, segment.data.length);
                    offset += segment.data.length;
                }

                if (segment.xmit >= deadlink) {
                    state = -1; // state = 0(c#)
                }
            }
        }

        // flash remain segments
        if (offset > 0) {
            output(buffer, offset);
        }

        // update ssthresh
        if (change != 0) {
        	int inflight = snd_nxt - sndUna;
            ssthresh = inflight / 2;
            if (ssthresh < IKCP_THRESH_MIN) {
                ssthresh = IKCP_THRESH_MIN;
            }
            cwnd = ssthresh + resent;
            incr = cwnd * mss;
        }

        if (lost != 0) {
            ssthresh = cwnd / 2;
            if (ssthresh < IKCP_THRESH_MIN) {
                ssthresh = IKCP_THRESH_MIN;
            }
            cwnd = 1;
            incr = mss;
        }

        if (cwnd < 1) {
            cwnd = 1;
            incr = mss;
        }
    }

    // update state (call it repeatedly, every 10ms-100ms), or you can ask
    // ikcp_check when to call it again (without ikcp_input/_send calling).
    // 'current' - current timestamp in millisec.
    public void update(long current_) {

        current = current_;

        if (0 == updated) {
            updated = 1;
            tsflush = current;
        }

        int slap = itimediff(current, tsflush);

        if (slap >= 10000 || slap < -10000) {
            tsflush = current;
            slap = 0;
        }

        if (slap >= 0) {
            tsflush += interval;
            if (itimediff(current, tsflush) >= 0) {
                tsflush = current + interval;
            }
            flush();
        }
    }

    // Determine when should you invoke ikcp_update:
    // returns when you should invoke ikcp_update in millisec, if there
    // is no ikcp_input/_send calling. you can call ikcp_update in that
    // time, instead of call update repeatly.
    // Important to reduce unnacessary ikcp_update invoking. use it to
    // schedule ikcp_update (eg. implementing an epoll-like mechanism,
    // or optimize ikcp_update when handling massive kcp connections)
    public long check(long current_) {

        long ts_flush_ = tsflush;
        long tm_flush;
        int tm_packet = 0x7fffffff;
        long minimal;

        if (0 == updated) {
            return current_;
        }

        if (itimediff(current_, ts_flush_) >= 10000 || itimediff(current_, ts_flush_) < -10000) {
            ts_flush_ = current_;
        }

        if (itimediff(current_, ts_flush_) >= 0) {
            return current_;
        }

        tm_flush = itimediff(ts_flush_, current_);

        for (Segment seg : nsndBuf) {
            int diff = itimediff(seg.resendts, current_);
            if (diff <= 0) {
                return current_;
            }
            if (diff < tm_packet) {
                tm_packet = diff;
            }
        }

        minimal = tm_packet < tm_flush ? tm_packet : tm_flush;
        if (minimal >= interval) {
            minimal = interval;
        }
        return current_ + minimal;
    }

    // change MTU size, default is 1400
    public int setMtu(int mtu_) {
        if (mtu_ < 50 || mtu_ < (int) IKCP_OVERHEAD) {
            throw new IllegalArgumentException("mtu must >=50");
        }
        byte[] buffer_ = new byte[(mtu_ + IKCP_OVERHEAD) * 3];
        mtu = (int) mtu_;
        mss = mtu - IKCP_OVERHEAD;
        buffer = buffer_;
        return 0;
    }

    public int interval(int interval_) {
        if (interval_ > 5000) {
            interval_ = 5000;
        } else if (interval_ < 10) {
            interval_ = 10;
        }
        interval = (int) interval_;
        return 0;
    }

    // fastest: ikcp_nodelay(kcp, 1, 20, 2, 1)
    // nodelay: 0:disable(default), 1:enable
    // interval: internal update timer interval in millisec, default is 100ms
    // resend: 0:disable fast resend(default), 1:enable fast resend
    // nc: 0:normal congestion control(default), 1:disable congestion control
    public int noDelay(int nodelay_, int interval_, int resend_, int nc_) {

        if (nodelay_ > 0) {
            nodelay = nodelay_;
            if (nodelay_ != 0) {
                rxminrto = IKCP_RTO_NDL;
            } else {
                rxminrto = IKCP_RTO_MIN;
            }
        }

        if (interval_ >= 0) {
            if (interval_ > 5000) {
                interval_ = 5000;
            } else if (interval_ < 10) {
                interval_ = 10;
            }
            interval = interval_;
        }

        if (resend_ >= 0) {
            fastresend = resend_;
        }

        if (nc_ >= 0) {
            nocwnd = nc_;
        }

        return 0;
    }

    // set maximum window size: sndwnd=32, rcvwnd=32 by default
    public int wndSize(int sndwnd, int rcvwnd) {
        if (sndwnd > 0) {
            snd_wnd = (int) sndwnd;
        }

        if (rcvwnd > 0) {
            rcvWnd = (int) rcvwnd;
        }
        return 0;
    }

    // get how many packet is waiting to be sent
    public int waitSnd() {
        return nsndBuf.size() + nsndque.size();
    }
}