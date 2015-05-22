package jazmin.server.cdn;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * 
 * @author yama
 *
 */
public class CdnServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static Logger logger=LoggerFactory.get(CdnServerHandler.class);
	//
	CdnServer cdnServer;
	public CdnServerHandler(CdnServer server) {
		this.cdnServer=server;
	}
	//
    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject request) 
    		throws Exception {
    	cdnServer.processRequest(ctx, request);
    }
    //
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	if(cause instanceof IOException){
    		logger.warn(cause.getMessage());
    	}else{
    		logger.catching(cause);
    	}
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	RequestWorker rw=ctx.channel().attr(CdnServer.WORKER_KEY).get();
    	if(rw!=null){
    		rw.channelClosed();
    	}
    }
}