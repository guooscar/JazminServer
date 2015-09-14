
package jazmin.server.rpc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * @date Jun 4, 2014
 */
@Sharable
public class RpcServerHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(RpcServerHandler.class);
	private static final AttributeKey<RpcSession> SESSION_KEY=
											AttributeKey.valueOf("rpcsession");
	private RpcServer rpcServer;
	public RpcServerHandler(RpcServer rpcServer) {
		this.rpcServer=rpcServer;
	}
	/*
	 * destroy session when connection closed
	 */
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		RpcSession session=ctx.channel().attr(SESSION_KEY).get();
		rpcServer.sessionDestroyed(session);
	}
	/*
	 * 
	 */
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
		RpcMessage rpcMessage=(RpcMessage) msg;
		RpcSession session=ctx.channel().attr(SESSION_KEY).get();
		session.receivePackage();
		rpcServer.messageReceived(session, rpcMessage);
	}
	/*
	 * 
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(evt instanceof IdleStateEvent){  
			RpcSession session=ctx.channel().attr(SESSION_KEY).get();
			if(logger.isWarnEnabled()){
				logger.warn("close idle session:{}",session);
			}
			ctx.close();
		}    
	}
	/*
     * 
     */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("rpc session create:{}",ctx.channel());
		RpcSession session=new RpcSession();
		session.setChannel(ctx.channel());
		ctx.channel().attr(SESSION_KEY).set(session);
		rpcServer.checkSession(session);
	}
    /*
     * 
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    		throws Exception {
    	if((cause instanceof IOException)||(cause instanceof DecoderException)){
    		logger.warn("exception on channal:"+ctx.channel()+","+cause.getMessage());
    	}else{
    		logger.error("exception on channal:"+ctx.channel(),cause);	
    	}
        ctx.close();
    }
}
