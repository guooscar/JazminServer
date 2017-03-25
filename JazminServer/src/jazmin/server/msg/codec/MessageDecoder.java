/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.CodecFactory;
/**
 * @author yama
 * 26 Dec, 2014
 */
public class MessageDecoder extends ByteToMessageDecoder {
	private static Logger logger=LoggerFactory.get(MessageDecoder.class);
	
	private CodecFactory codecFactory;
	NetworkTrafficStat networkTrafficStat;
	public MessageDecoder(CodecFactory codecFactory,NetworkTrafficStat networkTrafficStat) {
		this.codecFactory=codecFactory;
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		try{
			RequestMessage msg=codecFactory.decode(in, networkTrafficStat);
			if(msg!=null){
				out.add(msg);
			}	
		}catch (Exception e) {
			logger.catching(e);
		}
	}
}
