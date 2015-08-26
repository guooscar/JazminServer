/**
 * 
 */
package jazmin.server.webssh;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.IOWorker;

/**
 * @author yama
 * 26 Aug, 2015
 */
public class WebSshServer extends Server{
	private int port=9001;
	private  ServerBootstrap webSocketServerBootstrap;
	private  EventLoopGroup bossGroup;
	private  EventLoopGroup workerGroup;
	private  Map<String, WebSshChannel>channels;
	   
	 
	//
	public WebSshServer() {
		channels=new ConcurrentHashMap<String, WebSshChannel>();
	}
	//
	//
	void addChannel(WebSshChannel c){
		channels.put(c.id, c);
	}
	//
	void removeChannel(String id){
		channels.remove(id);
	}
	//
	private void createWSListeningPoint()throws Exception{
		IOWorker ioWorker=new IOWorker("WebTtyServerWorker",
    			Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup=new NioEventLoopGroup(1,ioWorker);
    	workerGroup=new NioEventLoopGroup(0,ioWorker);
        final ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(3600,3600,0));
    			ch.pipeline().addLast(new HttpServerCodec());
    			ch.pipeline().addLast(new HttpObjectAggregator(65536));
    			ch.pipeline().addLast(new WebSocketServerCompressionHandler());
    			ch.pipeline().addLast(new WebSshWebSocketHandler(WebSshServer.this));
                pipeline.addLast("handler", new WebSshWebSocketHandler(WebSshServer.this));
            }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true);
        //
        webSocketServerBootstrap=b;
        webSocketServerBootstrap.bind(port).sync();
    }
	//--------------------------------------------------------------------------
	//
	@Override
	public void start() throws Exception {
		createWSListeningPoint();
	}
	//
	@Override
	public String info() {
		return "";
	}
	//
	public static void main(String[] args) {
		LoggerFactory.setLevel("DEBUG");
		WebSshServer server=new WebSshServer();
		Jazmin.addServer(server);
		Jazmin.start();
	}
}
