/**
 * 
 */
package jazmin.server.sip;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.pkts.buffer.Buffer;

import java.net.InetSocketAddress;

import jazmin.server.sip.stack.Connection;
import jazmin.server.sip.stack.SipMessageDatagramDecoder;
import jazmin.server.sip.stack.SipMessageEncoder;
import jazmin.server.sip.stack.SipMessageStreamDecoder;
import jazmin.server.sip.stack.UdpConnection;

/**
 * @author yama
 *
 */
public class SipServer {

	private String ip;
    private int port;
    private  EventLoopGroup bossGroup;
    private  EventLoopGroup workerGroup;
    private  EventLoopGroup udpGroup;
    private  SipHandler handler;
    private  ServerBootstrap serverBootstrap;
    private  Bootstrap bootstrap;
    private Channel udpListeningPoint = null;
    //
    public SipServer() {
        this.ip = "127.0.0.1";
        this.port = 5060;
    }
  
    //
    public Connection connect(final String ip, final int port) {
        final InetSocketAddress remoteAddress = new InetSocketAddress(ip, port);
        return new UdpConnection(this.udpListeningPoint, remoteAddress);
    }
    //
    public Connection connect(final Buffer ip, final int port) {
        return connect(ip.toString(), port);
    }
    //
    private void startNetty() throws Exception {
        try {
            final InetSocketAddress socketAddress = new InetSocketAddress(this.ip, this.port);
            this.udpListeningPoint = this.bootstrap.bind(socketAddress).sync().channel();
            this.serverBootstrap.bind(socketAddress).sync().channel().closeFuture().await();
        } finally {
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
            this.udpGroup.shutdownGracefully();
        }
    }
    //
    private Bootstrap createUDPListeningPoint() {
        final Bootstrap b = new Bootstrap();
        b.group(this.udpGroup)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(final DatagramChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new SipMessageDatagramDecoder());
                pipeline.addLast("encoder", new SipMessageEncoder());
                pipeline.addLast("handler", handler);
            }
        });
        return b;
    }
    //
    private ServerBootstrap createTCPListeningPoint() {
        final ServerBootstrap b = new ServerBootstrap();

        b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new SipMessageStreamDecoder());
                pipeline.addLast("encoder", new SipMessageEncoder());
                pipeline.addLast("handler", handler);
            }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true);
        return b;
    }
    //-------------------------------------------------------------------------
    //
    public void start() throws Exception{
    	handler=new SipHandler();
    	bossGroup=new NioEventLoopGroup();
    	workerGroup=new NioEventLoopGroup();
    	udpGroup=new NioEventLoopGroup();
        this.bootstrap = createUDPListeningPoint();
        this.serverBootstrap = createTCPListeningPoint();
        startNetty();
    }
	//--------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		SipServer server=new SipServer();
		server.start();
	}

}
