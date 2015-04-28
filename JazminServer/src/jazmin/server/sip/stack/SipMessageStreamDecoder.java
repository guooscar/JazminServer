/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipInitialLine;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipResponseImpl;

/**
 * @author jonas
 * 
 */
public class SipMessageStreamDecoder extends ByteToMessageDecoder {

	private static Logger logger = LoggerFactory
			.get(SipMessageStreamDecoder.class);
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
	protected void decode(final ChannelHandlerContext ctx,
			final ByteBuf buffer, final List<Object> out) throws Exception {
		while (!this.message.isComplete() && buffer.isReadable()) {
			final byte b = buffer.readByte();
			this.message.write(b);
		}
		//
		if (this.message.isComplete()) {

			final long arrivalTime = this.clock.getCurrentTimeMillis();
			SipMessage sipMessage = null;
			Buffer rawInitialLine = message.getInitialLine();
			if (SipInitialLine.isResponseLine(rawInitialLine)) {
				sipMessage = new SipResponseImpl(rawInitialLine,
						message.getHeaders(), message.getPayload());
			} else {
				sipMessage = new SipRequestImpl(rawInitialLine,
						message.getHeaders(), message.getPayload());
			}

			if (logger.isDebugEnabled()) {
				logger.debug("<<<<<<<<<<receive from {}\n{}", ctx.channel(),
						sipMessage);
			}
			final Channel channel = ctx.channel();
			final Connection connection = new TcpConnection(channel,
					(InetSocketAddress) channel.remoteAddress());
			out.add(new DefaultSipMessageEvent(connection, sipMessage,
					arrivalTime));
			reset();
		}
	}
	//
	private void reset() {
		this.message = new RawMessage(MAX_ALLOWED_INITIAL_LINE_SIZE,
				MAX_ALLOWED_HEADERS_SIZE, MAX_ALLOWED_CONTENT_LENGTH);
	}

}
