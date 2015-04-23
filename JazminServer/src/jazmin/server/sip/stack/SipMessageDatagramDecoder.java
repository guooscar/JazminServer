package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.impl.SipParser;

import java.util.List;

/**
 * The {@link SipMessageDatagramDecoder} will frame an incoming UDP packet into
 * a {@link SipMessage}. Since the data will only be framed, only very minimal
 * checking of whether the data is actually a valid SIP message or not will be
 * performed. It is up to the user to validate the SipMessage through the method
 * {@link SipMessage#verify()}. The philosophy is to simply just frame things as
 * fast as possible and then do lazy parsing as much as possible.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipMessageDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private final Clock clock;

    public SipMessageDatagramDecoder() {
        this.clock = new SystemClock();
    }

    public SipMessageDatagramDecoder(final Clock clock) {
        this.clock = clock;
    }

    /**
     * Framing an UDP packet is much simpler than for a stream based protocol
     * like TCP. We just assumes that everything is correct and therefore all is
     * needed is to read the first line, which is assumed to be a SIP initial
     * line, then read all headers as one big block and whatever is left better
     * be the payload (if there is one).
     * 
     * Of course, things do go wrong. If e.g. the UDP packet is fragmented, then
     * we may end up with a partial SIP message but the user can either decide
     * to double check things by calling {@link SipMessage#verify()} or the user
     * will eventually notice when trying to access partial headers etc.
     * 
     */
    @Override
    protected void decode(final ChannelHandlerContext ctx, final DatagramPacket msg, final List<Object> out)
            throws Exception {
        final long arrivalTime = this.clock.getCurrentTimeMillis();
        final ByteBuf content = msg.content();

        // some clients are sending various types of pings even over
        // UDP, such as linphone which is sending "jaK\n\r".
        // According to RFC5626, the only valid ping over UDP
        // is to use a STUN request and since such a request is
        // at least 20 bytes we will simply ignore anything less
        // than that. And yes, there is no way that an actual
        // SIP message ever could be less than 20 bytes.
        if (content.readableBytes() < 20) {
            return;
        }

        final byte[] b = new byte[content.readableBytes()];
        content.getBytes(0, b);

        final Buffer buffer = Buffers.wrap(b);
        SipParser.consumeSWS(buffer);
        final SipMessage sipMessage = SipParser.frame(buffer);
        // System.err.println("CSeq header: " + sipMessage.getCSeqHeader());

        // final SipInitialLine initialLine = SipInitialLine.parse(buffer.readLine());
        // final Buffer headers = buffer.readUntilDoubleCRLF();
        // SipMessage sipMessage = null;
        // if (initialLine.isRequestLine()) {
        // sipMessage = new SipRequestImpl(initialLine.toRequestLine(), headers, buffer);
        // } else {
        // sipMessage = new SipResponseImpl(initialLine.toResponseLine(), headers, buffer);
        // }

        final Connection connection = new UdpConnection(ctx.channel(), msg.sender());
        final SipMessageEvent event = new DefaultSipMessageEvent(connection, sipMessage, arrivalTime);
        out.add(event);
    }

}
