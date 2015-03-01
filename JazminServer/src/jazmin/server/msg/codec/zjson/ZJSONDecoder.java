package jazmin.server.msg.codec.zjson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.JSONRequestParser;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class ZJSONDecoder extends ByteToMessageDecoder {
	private static Logger logger = LoggerFactory.get(ZJSONDecoder.class);
	// max input message length
	private static final int MAX_MESSAGE_LENGTH = 1024 * 10;	
	NetworkTrafficStat networkTrafficStat;
	public ZJSONDecoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
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
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		networkTrafficStat.inBound(dataLength);
		byte[] decoded = new byte[dataLength];
		in.readBytes(decoded);
		byte[] bb = IOUtil.decompress(decoded);
		String s = new String(bb, "UTF-8");
		RequestMessage reqMessage=JSONRequestParser.createRequestMessage(s);
		if (logger.isDebugEnabled()) {
			logger.debug("\ndecode message--------------------------------------\n{} "
					+ "\nzjson decode size:{}",
					DumpUtil.formatJSON(s),
					decoded.length+"/"+bb.length);
		}
		out.add(reqMessage);
	}

	
}
