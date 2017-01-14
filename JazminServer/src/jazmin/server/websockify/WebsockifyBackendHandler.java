package jazmin.server.websockify;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
/**
 * 
 * @author yama
 *
 */
public class WebsockifyBackendHandler extends ChannelHandlerAdapter {
	private static Logger logger=LoggerFactory.get(WebsockifyBackendHandler.class);
    private final Channel inboundChannel;
    public WebsockifyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
    	BinaryWebSocketFrame frame=new BinaryWebSocketFrame(((ByteBuf) msg).retain());
    	inboundChannel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    	 inboundChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
    			 ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.catching(cause);
    	ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
    			ChannelFutureListener.CLOSE);
    }
}