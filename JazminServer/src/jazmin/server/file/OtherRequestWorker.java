/**
 * 
 */
package jazmin.server.file;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import jazmin.core.thread.NoTraceLog;


/**
 * @author yama
 *
 */
public class OtherRequestWorker extends RequestWorker{
	
	OtherRequestWorker(FileServer cdnServer, ChannelHandlerContext ctx,
			DefaultHttpRequest request) {
		super(cdnServer, ctx, request);
	}
	@NoTraceLog
	public void processRequest(){
		sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	public void handleHttpContent(DefaultHttpContent content) {
		
	}

	@Override
	public void channelClosed() {
		
	}
	
}
