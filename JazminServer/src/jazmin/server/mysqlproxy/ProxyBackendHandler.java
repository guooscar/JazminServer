package jazmin.server.mysqlproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.mysqlproxy.mysql.proto.Handshake;
import jazmin.util.DumpUtil;
import jazmin.util.HexDumpUtil;
/**
 * 
 * @author yama
 *
 */
public class ProxyBackendHandler extends ChannelHandlerAdapter {
	private static Logger logger=LoggerFactory.get(ProxyFrontendHandler.class);
	//
    private final Channel inboundChannel;
    private Handshake handshake;
    //
    public ProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg)throws Exception {
        byte[] packet = (byte[]) msg;
        if(handshake==null) {
        	handshake=Handshake.loadFromPacket(packet);
        	System.err.println(DumpUtil.dump(handshake));
        	
        }
        System.err.println("<----\n"+HexDumpUtil.dumpHexString(packet));
        writeToFrontend(ctx, packet);
        
    }
    //
    private void writeToFrontend(ChannelHandlerContext ctx, byte[] packet) {
    	ByteBuf buffer = ctx.alloc().buffer(packet.length);
        buffer.writeBytes(packet);
        inboundChannel.writeAndFlush(buffer).addListener(new ChannelFutureListener() {
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
        ProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.catching(cause);
        ProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}