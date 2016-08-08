package jazmin.server.file;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

/**
 * Handler that just dumps the contents of the response from the server
 */
public class HttpUploadClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static Logger logger=LoggerFactory.get(HttpUploadClientHandler.class);
	public static final  AttributeKey<String> ATTR_RESULT=AttributeKey.valueOf("r");
	//
    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            String response=(chunk.content().toString(CharsetUtil.UTF_8));
            if(!response.isEmpty()){
                ctx.channel().attr(ATTR_RESULT).set(response);
            }
            ctx.channel().close();    	
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	ctx.channel().close();
    }
    //
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.catching(cause);
        ctx.channel().close();
    }
}