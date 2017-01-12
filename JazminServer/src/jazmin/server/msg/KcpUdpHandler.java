/**
 * 
 */
package jazmin.server.msg;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class KcpUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	private static Logger logger=LoggerFactory.get(KcpUdpHandler.class);
	NettyKcpChannelManager kcpSessionManager;
	
	public KcpUdpHandler(MessageServer messageServer) {
		kcpSessionManager=new NettyKcpChannelManager(messageServer);
	}
	//
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			DatagramPacket pkg) throws Exception {
		kcpSessionManager.receiveDatagramPacket(ctx.channel(),pkg);
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
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channel active :"+ctx.channel());
	}
	//
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channel inactive :"+ctx.channel());
	}
}

