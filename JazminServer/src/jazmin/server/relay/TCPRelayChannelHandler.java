/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class TCPRelayChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
	private static Logger logger=LoggerFactory.get(RelayChannel.class);
	//
	TCPRelayChannel relayChannel;
	public TCPRelayChannelHandler(TCPRelayChannel relayChannel) {
		this.relayChannel=relayChannel;
	}
	//
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			ByteBuf buffer) throws Exception {
		ByteBuf buf= Unpooled.copiedBuffer(buffer);
		relayChannel.dataFromPeer(
				(InetSocketAddress) ctx.channel().remoteAddress(),
				buf.array());
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
		if(relayChannel.remoteAddress==null){
			relayChannel.outboundChannel=ctx.channel();
			relayChannel.remoteAddress=(InetSocketAddress) ctx.channel().remoteAddress();
		}
		logger.info("channel active :"+ctx.channel());
	}
	//
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		relayChannel.remoteAddress=null;
		logger.info("channel inactive :"+ctx.channel());
	}
}
