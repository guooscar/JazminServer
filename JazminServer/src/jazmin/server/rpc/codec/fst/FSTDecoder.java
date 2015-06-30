package jazmin.server.rpc.codec.fst;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RpcMessage;

import org.nustaq.serialization.FSTConfiguration;

/**
 * @author yama
 * @date Jun 7, 2014
 */
public class FSTDecoder extends ByteToMessageDecoder {
	private static Logger logger=LoggerFactory.get(FSTDecoder.class);
	private static final int MAX_MESSAGE_LENGTH = 1024 * 1024*10;
	//
	NetworkTrafficStat networkTrafficStat;
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	//
	public FSTDecoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	/**
	 * 
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// Wait until the length prefix is available.
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (dataLength > MAX_MESSAGE_LENGTH) {
			in.resetReaderIndex();
			logger.error("message too long " + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
			throw new DecoderException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		networkTrafficStat.inBound(dataLength);
		byte[] decoded = new byte[dataLength];
		in.readBytes(decoded);
		//
		RpcMessage msg;
		msg=(RpcMessage) conf.asObject(decoded);
		out.add(msg);
	}
}
