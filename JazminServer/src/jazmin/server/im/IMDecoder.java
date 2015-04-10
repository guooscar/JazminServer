package jazmin.server.im;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class IMDecoder extends ByteToMessageDecoder {
	private static Logger logger = LoggerFactory.get(IMDecoder.class);
	// max input message size 
	private static final short MAX_MESSAGE_LENGTH = Short.MAX_VALUE;	
	NetworkTrafficStat networkTrafficStat;
	public IMDecoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	/**
	 * 
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// Wait until the length prefix is available.
		if (in.readableBytes() < 2) {
			return;
		}
		in.markReaderIndex();
		int dataLength=in.readShort();
		//
		if (dataLength > MAX_MESSAGE_LENGTH) {
			in.resetReaderIndex();
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		//read next bytes
		if (in.readableBytes() < (dataLength-2)) {
			in.resetReaderIndex();
			return;
		}
		networkTrafficStat.inBound(dataLength);
		byte[] decoded = new byte[dataLength];
		in.resetReaderIndex();
		in.readBytes(decoded);
		int serviceId=(decoded[2]<<8)|decoded[3]&0xff;
		
		if (logger.isDebugEnabled()) {
			logger.debug("decode message serviceId:0x{},length:{} ",
					Integer.toHexString(serviceId),
					decoded.length);
		}
		IMRequestMessage requestMessage=new IMRequestMessage();
		requestMessage.serviceId=serviceId;
		requestMessage.rawData=decoded;
		out.add(requestMessage);
	}

	
}
