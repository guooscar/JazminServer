
package jazmin.server.rpc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * @date Jun 4, 2014
 */
@Sharable
public class RPCServerHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(RPCServerHandler.class);
	private static final AttributeKey<RPCSession> SESSION_KEY=
											AttributeKey.valueOf("rpcsession");
	private RPCServer rpcServer;
	public RPCServerHandler(RPCServer rpcServer) {
		this.rpcServer=rpcServer;
	}
	/*
	 * destroy session when connection closed
	 */
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		RPCSession session=ctx.channel().attr(SESSION_KEY).get();
		rpcServer.sessionDestroyed(session);
	}
	/*
	 * 
	 */
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
		RPCMessage rpcMessage=(RPCMessage) msg;
		RPCSession session=ctx.channel().attr(SESSION_KEY).get();
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
			RPCSession session=ctx.channel().attr(SESSION_KEY).get();
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
		RPCSession session=new RPCSession();
		session.channel(ctx.channel());
		ctx.channel().attr(SESSION_KEY).set(session);
	}
    /*
     * 
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    		throws Exception {
    	logger.error("exception caught from:"+ctx.channel(),cause);
        ctx.close();
    }
}
