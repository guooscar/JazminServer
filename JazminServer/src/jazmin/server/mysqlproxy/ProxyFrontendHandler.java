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

import java.nio.ByteBuffer;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.mysqlproxy.MySQLProxyServer.ProxyServerBackendChannelInitializer;
import jazmin.server.mysqlproxy.mysql.protocol.AuthPacket;
import jazmin.server.mysqlproxy.mysql.protocol.Capabilities;
/**
 * 
 * @author yama
 *
 */
public class ProxyFrontendHandler extends ChannelHandlerAdapter {
	private static Logger logger=LoggerFactory.get(ProxyFrontendHandler.class);
    private volatile Channel outboundChannel;
    private MySQLProxyServer server;
    private AuthPacket authPacket;
    ProxyRule rule;
    ProxySession session;
    //
    public ProxyFrontendHandler(MySQLProxyServer server,ProxyRule rule) {
    	this.server=server;
    	this.rule=rule;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        String remoteHost=rule.remoteHost;
        int remotePort=rule.remotePort;
        if(logger.isDebugEnabled()){
        	logger.debug("connect to backend {}:{}",remoteHost,remotePort);
        }
        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
         .channel(ctx.channel().getClass())
         .handler(new ProxyServerBackendChannelInitializer(server,rule,this,inboundChannel))
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
                    inboundChannel.read();
                } else {
                	if(logger.isDebugEnabled()){
                    	logger.debug("connect to backend {}:{} error",
                    			remoteHost,remotePort);
                    }
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
    	byte[] packet = (byte[]) msg;
    	//System.err.println("----->\n"+HexDumpUtil.dumpHexString(packet));
    	if(authPacket==null&&rule.authProvider!=null) {
    		authPacket=new AuthPacket();
    		authPacket.read(packet);
    		session.clientPassword=authPacket.password;
    		session.user=authPacket.user;
    		session.dbUser=authPacket.user;
    		//
    		Database database=rule.authProvider.auth(session);
    		if(database!=null){
    			//rewrite user and password
    			authPacket.password=ProxySession.scramble411(database.password.getBytes(),session.challenge);
    			authPacket.user=database.user;
    			if((authPacket.clientFlags & Capabilities.CLIENT_CONNECT_ATTRS) != 0){
    				authPacket.clientFlags^=Capabilities.CLIENT_CONNECT_ATTRS;
    			}
    			//log
    			session.dbUser=database.user;
    			//
    			ByteBuffer bf=ByteBuffer.allocate(authPacket.calcPacketSize()+4);
        		authPacket.write(bf);
        		//System.err.println("C----->\n"+HexDumpUtil.dumpHexString(bf.array()));
        		writeToBackend(ctx,bf.array());
        		return;
    		}
    	}
    	if(session!=null){
    		session.packetCount++;
        }
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
        	logger.debug("disconnect from frontend {}",ctx.channel());
        }
    	if(session!=null){
    		server.removeSession(session.id);	
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