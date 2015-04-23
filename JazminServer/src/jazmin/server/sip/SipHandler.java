package jazmin.server.sip;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.pkts.packet.sip.SipMessage;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.stack.SipMessageEvent;
/**
 * 
 * @author yama
 *
 */
@Sharable
public final class SipHandler extends SimpleChannelInboundHandler<SipMessageEvent> {
	//
	private static Logger logger=LoggerFactory.get(SipHandler.class);
	//
	@Override
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
		logger.debug("channelInactive:"+ctx.channel());	
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) 
			throws Exception {
		logger.debug("channelActive:"+ctx.channel());	
	}
	//
	@Override
	public void messageReceived(ChannelHandlerContext ctx,
			SipMessageEvent event) throws Exception {
		 final SipMessage msg = event.getMessage();
		 logger.debug(msg); 
	}
	 //
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    		throws Exception {
    	if(cause instanceof IOException){
    		logger.warn("exception on channal:"+ctx.channel()+","+cause.getMessage());
    	}else{
    		logger.error("exception on channal:"+ctx.channel(),cause);	
    	}
    	ctx.close();
    }
}