/**
 * 
 */
package jazmin.server.cdn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;


/**
 * @author yama
 *
 */
public class OtherRequestWorker extends RequestWorker{
	
	OtherRequestWorker(CdnServer cdnServer, ChannelHandlerContext ctx,
			FullHttpRequest request) {
		super(cdnServer, ctx, request);
	}

	public void processRequest(){
		sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
	}
	
}
