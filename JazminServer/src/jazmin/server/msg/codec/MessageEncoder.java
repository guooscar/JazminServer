package jazmin.server.msg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.CodecFactory;
/**
 * 
 * @author yama
 *
 */
@Sharable
public class MessageEncoder extends MessageToByteEncoder<ResponseMessage> {
	private static Logger logger=LoggerFactory.get(MessageEncoder.class);
	//
	private CodecFactory codecFactory;
	NetworkTrafficStat networkTrafficStat;
	
	public MessageEncoder(CodecFactory codecFactory,NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	    this.codecFactory=codecFactory;
	}
	
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			ResponseMessage msg,
			ByteBuf out) throws Exception {
		try{
			codecFactory.encode(msg, out, networkTrafficStat);				
		}catch (Exception e) {
			logger.catching(e);
		}
	}
}
