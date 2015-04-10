/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.im;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;

/**
 * 
 * @author yama
 * @date Jun 8, 2014
 */
@Sharable
public class IMEncoder extends MessageToByteEncoder<IMResponseMessage> {
	private static Logger logger=LoggerFactory.get(IMEncoder.class);
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	NetworkTrafficStat networkTrafficStat;
	public IMEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			IMResponseMessage msg,
			ByteBuf out) throws Exception {
		
		byte compressBytes[] = msg.rawData;
		int dataLength=compressBytes.length;
		if(logger.isDebugEnabled()){
    		logger.debug("encode message length:{}",
					compressBytes.length);
    	}
    	
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		networkTrafficStat.outBound(dataLength);
		out.writeBytes(compressBytes);
	}
	
}
