package jazmin.server.rpc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class RPCClientHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(RPCClientHandler.class);
	private RPCClient rpcClient;
    /**
     */
    public RPCClientHandler(RPCClient rpcClient) {
    	this.rpcClient=rpcClient;
 	}	
    /**
     * 
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	if(logger.isInfoEnabled()){
    		logger.info("rpc client close.{}",ctx.channel());
    	}
    }
    /**
     * 
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	if(logger.isInfoEnabled()){
			logger.info("rpc client registered.{}",ctx.channel());		
		}	
    }
    /**
     * 
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
    	RPCMessage rpcMessage=(RPCMessage)msg;
    	RPCSession session=ctx.channel().attr(RPCClient.SESSION_KEY).get();
    	session.receivePackage();
    	rpcClient.messageRecieved(session,rpcMessage);
    }
    /**
     * 
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    		throws Exception {
    	logger.error("exception caught from:"+ctx.channel(),cause);
    	RPCSession session=ctx.channel().attr(RPCClient.SESSION_KEY).get();
    	session.close();
        ctx.close();
    }

}
