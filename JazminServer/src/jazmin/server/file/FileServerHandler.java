package jazmin.server.file;
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
public class FileServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static Logger logger=LoggerFactory.get(FileServerHandler.class);
	//
	FileServer fileServer;
	public FileServerHandler(FileServer server) {
		this.fileServer=server;
	}
	//
    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject request) 
    		throws Exception {
    	fileServer.processRequest(ctx, request);
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
    	RequestWorker rw=ctx.channel().attr(FileServer.WORKER_KEY).get();
    	if(rw!=null){
    		rw.channelClosed();
    	}
    }
}