package jazmin.server.sip;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.stack.SipMessageEvent;
/**
 * 
 * @author yama
 *
 */
@Sharable
public final class SipSocketHandler extends SimpleChannelInboundHandler<SipMessageEvent> {
	//
	private static Logger logger=LoggerFactory.get(SipSocketHandler.class);
	
	SipServer sipServer;
	public SipSocketHandler(SipServer server){
		this.sipServer=server;
	}
	//
	@Override
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
		sipServer.removeChannel(ctx.channel().id().asShortText());
		if(logger.isDebugEnabled()){
			logger.debug("channelInactive:"+ctx.channel());	
		}
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) 
			throws Exception {
		SipChannel channel=new SipChannel();
		if(ctx.channel() instanceof NioSocketChannel){
			channel.transport="tcp";
		}else{
			channel.transport="udp";		
		}
		SslHandler sslHandler=ctx.pipeline().get(SslHandler.class);
		if(sslHandler!=null){
			channel.transport="tls";
			if(logger.isDebugEnabled()){
				sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
					public void operationComplete(Future<? super Channel> arg0) throws Exception {
						String ciperSuite=ctx.pipeline().get(SslHandler.class).engine()
							.getSession().getCipherSuite();
						logger.debug("handshake complete."+ctx.channel()+"/"+ciperSuite);
					};
				});
			}
		}
		channel.id=ctx.channel().id().asShortText();
		InetSocketAddress sa=(InetSocketAddress) ctx.channel().localAddress();
		channel.localAddress=sa.getAddress().getHostAddress();
		channel.localPort=sa.getPort();
		//
		sa=(InetSocketAddress) ctx.channel().remoteAddress();
		if(sa!=null){
			channel.remoteAddress=sa.getAddress().getHostAddress();
			channel.remotePort=sa.getPort();	
		}
		sipServer.addChannel(channel);
		ctx.channel().attr(SipChannel.SESSION_KEY).set(channel);
		//
		if(logger.isDebugEnabled()){
			logger.debug("channelActive:"+ctx.channel());	
		}
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
	//--------------------------------------------------------------------------
	@Override
	public void messageReceived(
			ChannelHandlerContext ctx,
			SipMessageEvent event) throws Exception {
		SipChannel c=ctx.channel().attr(SipChannel.SESSION_KEY).get();
		c.messageReceivedCount++;
		sipServer.messageReceived(event.getConnection(),event.getMessage());
	}
}