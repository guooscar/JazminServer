package jazmin.server.mysqlproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.mysqlproxy.MySQLProxyServer.ProxyServerBackendChannelInitializer;
import jazmin.server.mysqlproxy.mysql.proto.HandshakeResponse;
import jazmin.util.DumpUtil;
import jazmin.util.HexDumpUtil;
/**
 * 
 * @author yama
 *
 */
public class ProxyFrontendHandler extends ChannelHandlerAdapter {
	private static Logger logger=LoggerFactory.get(ProxyFrontendHandler.class);
    private volatile Channel outboundChannel;
    private MySQLProxyServer server;
    private HandshakeResponse handshakeResponse;
    public ProxyFrontendHandler(MySQLProxyServer server) {
    	this.server=server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        ProxyRule rule=server.getRule();
        String remoteHost=rule.remoteHost;
        int remotePort=rule.remotePort;
        if(logger.isDebugEnabled()){
        	logger.debug("connect to backend {}:{}",remoteHost,remotePort);
        }
        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
         .channel(ctx.channel().getClass())
         .handler(new ProxyServerBackendChannelInitializer(inboundChannel))
         .option(ChannelOption.TCP_NODELAY, true);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                	if(logger.isDebugEnabled()){
                    	logger.debug("connect to backend {}:{} success",
                    			remoteHost,remotePort);
                    }
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                	if(logger.isDebugEnabled()){
                    	logger.debug("connect to backend {}:{} error",
                    			remoteHost,remotePort);
                    }
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
    	byte[] packet = (byte[]) msg;
    	if(handshakeResponse==null) {
    		handshakeResponse=HandshakeResponse.loadFromPacket(packet);
    		//handshakeResponse.
    		System.err.println(DumpUtil.dump(handshakeResponse));
    	}
    	System.err.println("----->\n"+HexDumpUtil.dumpHexString(packet));
        writeToBackend(ctx, packet);
    }
    //
    private void writeToBackend(ChannelHandlerContext ctx, byte[] packet) {
    	ByteBuf buffer = ctx.alloc().buffer(packet.length);
        buffer.writeBytes(packet);
    	outboundChannel.writeAndFlush(buffer).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }
    //
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    	if(logger.isDebugEnabled()){
        	logger.debug("disconnect from backend {}",ctx.channel());
        }
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.catching(cause);
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}