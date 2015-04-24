/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipInitialLine;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestLine;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipResponseImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipResponseLine;

/**
 * @author jonas
 * 
 */
public class SipMessageStreamDecoder extends ByteToMessageDecoder {
	
	private static Logger logger=LoggerFactory.get(SipMessageStreamDecoder.class);
    /**
     * The maximum allowed initial line. If we pass this threshold we will drop
     * the message and close down the connection (if we are using a connection
     * oriented protocol ie)
     */
    public static final int MAX_ALLOWED_INITIAL_LINE_SIZE = 1024;

    /**
     * The maximum allowed size of ALL headers combined (in bytes).
     */
    public static final int MAX_ALLOWED_HEADERS_SIZE = 8192;

    public static final int MAX_ALLOWED_CONTENT_LENGTH = 2048;

    private final Clock clock;

    /**
     * Contains the raw framed message.
     */
    private RawMessage message;

    /**
     * 
     */
    public SipMessageStreamDecoder(final Clock clock) {
        this.clock = clock;
        reset();
    }

    public SipMessageStreamDecoder() {
        this(new SystemClock());
    }

    @Override
    public boolean isSingleDecode() {
        return true;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out)
            throws Exception {
        try {
            while (!this.message.isComplete() && buffer.isReadable()) {
                final byte b = buffer.readByte();
                this.message.write(b);
            }
        } catch (final MaxMessageSizeExceededException e) {
            dropConnection(ctx, e.getMessage());
            // TODO: mark this connection as dead since the future
            // for closing this decoder may take a while to actually
            // do its job
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (this.message.isComplete()) {
            final long arrivalTime = this.clock.getCurrentTimeMillis();
            final SipMessage msg = toSipMessage(this.message);
            if(logger.isDebugEnabled()){
    			logger.debug("---------------------------------------------<\n{}",msg);
    		}
            final Channel channel = ctx.channel();
            final Connection connection = new TcpConnection(channel, (InetSocketAddress) channel.remoteAddress());
            out.add(new DefaultSipMessageEvent(connection, msg, arrivalTime));
            reset();
        }
    }

    private SipMessage toSipMessage(final RawMessage raw) throws SipParseException {
        final SipInitialLine initialLine = SipInitialLine.parse(raw.getInitialLine());
        final Buffer headers = raw.getHeaders();
        final Buffer payload = raw.getPayload();
        if (initialLine.isRequestLine()) {
            return new SipRequestImpl((SipRequestLine) initialLine, headers, payload);
        } else {
            return new SipResponseImpl((SipResponseLine) initialLine, headers, payload);
        }
    }

    private void dropConnection(final ChannelHandlerContext ctx, final String reason) {
    }

    private void reset() {
        this.message = new RawMessage(MAX_ALLOWED_INITIAL_LINE_SIZE, MAX_ALLOWED_HEADERS_SIZE,
                MAX_ALLOWED_CONTENT_LENGTH);
    }

}
