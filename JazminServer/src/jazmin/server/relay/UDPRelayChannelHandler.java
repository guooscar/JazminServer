/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class UDPRelayChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	private static Logger logger=LoggerFactory.get(RelayChannel.class);
	//
	UDPRelayChannel relayChannel;
	public UDPRelayChannelHandler(UDPRelayChannel relayChannel) {
		this.relayChannel=relayChannel;
	}
	//
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			DatagramPacket pkg) throws Exception {
		InetSocketAddress isa=pkg.sender();
		if(relayChannel.remoteAddress==null){
			relayChannel.remoteAddress=isa;
		}
		ByteBuf buf= Unpooled.copiedBuffer(pkg.content());
		relayChannel.dataFromPeer(buf.array());
	}
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
