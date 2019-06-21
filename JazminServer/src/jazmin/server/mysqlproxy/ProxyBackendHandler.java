package jazmin.server.mysqlproxy;

import java.util.Base64;
import java.util.Date;

import org.bouncycastle.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.mysqlproxy.mysql.protocol.HandshakePacket;
/**
 * 
 * @author yama
 *
 */
public class ProxyBackendHandler extends ChannelHandlerAdapter {
	private static Logger logger=LoggerFactory.get(ProxyFrontendHandler.class);
	//
    private final Channel inboundChannel;
    private HandshakePacket handshake;
    private MySQLProxyServer server;
    private ProxyRule rule;
    ProxyFrontendHandler frontendHander;
    //
    public ProxyBackendHandler(MySQLProxyServer server,ProxyRule rule,ProxyFrontendHandler frontendHander,Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
        this.server=server;
        this.rule=rule;
        this.frontendHander=frontendHander;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg)throws Exception {
        byte[] packet = (byte[]) msg;
        //System.err.println("<----\n"+HexDumpUtil.dumpHexString(packet));
        if(handshake==null) {
        	handshake=new HandshakePacket();
        	handshake.read(packet);
        	ProxySession session=new ProxySession();
        	session.id=Base64.getEncoder().encodeToString(handshake.seed);
        	session.createTime=new Date();
        	session.lastAccTime=new Date();
        	
        	session.remoteHost=rule.remoteHost;
        	session.remotePort=rule.remotePort;
        	session.localPort=rule.localPort;
        	session.challenge=Arrays.concatenate(handshake.seed,handshake.restOfScrambleBuff);
        	frontendHander.session=session;
        	server.addSession(session);
        }
        if(frontendHander.session!=null){
        	frontendHander.session.packetCount++;
        }
       
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
    	if(logger.isDebugEnabled()){
        	logger.debug("disconnect from backend {}",ctx.channel());
        }
    	if(frontendHander.session!=null){
    		server.removeSession(frontendHander.session.id);	
    	}
    	ProxyFrontendHandler.closeOnFlush(inboundChannel);
       
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.catching(cause);
        ProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}