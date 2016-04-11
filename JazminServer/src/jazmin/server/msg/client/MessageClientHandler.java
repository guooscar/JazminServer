package jazmin.server.msg.client;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.ResponseMessage;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class MessageClientHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(MessageClientHandler.class);
	private MessageClient messageClient;
    /**
     */
    public MessageClientHandler(MessageClient messageClient) {
    	this.messageClient=messageClient;
 	}	
    /**
     * 
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	if(logger.isInfoEnabled()){
    		logger.info("message client  close.{}",ctx.channel());
    	}
    }
    /**
     * 
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	if(logger.isInfoEnabled()){
			logger.info("message client registered.{}",ctx.channel());		
		}	
    }
    /**
     * 
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
    	ResponseMessage rspMessage=(ResponseMessage)msg;
    	messageClient.messageRecieved(rspMessage);
    }
    /**
     * 
     */
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
