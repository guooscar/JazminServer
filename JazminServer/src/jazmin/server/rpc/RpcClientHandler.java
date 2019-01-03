package jazmin.server.rpc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class RpcClientHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(RpcClientHandler.class);
	private RpcClient rpcClient;
    /**
     */
    public RpcClientHandler(RpcClient rpcClient) {
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
    	RpcMessage rpcMessage=(RpcMessage)msg;
    	RpcSession session=ctx.channel().attr(RpcClient.SESSION_KEY).get();
    	session.receivePackage(rpcMessage);
    	rpcClient.messageRecieved(session,rpcMessage);
    }
    /**
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
	    	RpcSession session=ctx.channel().attr(RpcClient.SESSION_KEY).get();
	    	session.close();
        ctx.close();
    }

}
