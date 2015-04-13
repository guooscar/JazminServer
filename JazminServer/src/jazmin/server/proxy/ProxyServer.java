/**
 * 
 */
package jazmin.server.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.misc.io.IOWorker;

/**
 * @author yama
 *
 */
public class ProxyServer extends Server{
	ServerBootstrap nettyServer;
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	ChannelInitializer<SocketChannel> channelInitializer;
	int port;
	List<ProxyRule>proxyRules;
	//
	public ProxyServer() {
		proxyRules=new ArrayList<ProxyRule>();
		port=9001;
	}
	/**
	 * add proxy rule 
	 * @param host the remote host address
	 * @param port the remote host port
	 */
	public void addRule(String host,int port){
		ProxyRule rule=new ProxyRule();
		rule.remoteHost=host;
		rule.remotePort=port;
		proxyRules.add(rule);
	}
	/**
	 * get next proxy rule
	 * @return
	 */
	public ProxyRule getRule(){
		return proxyRules.get(0);
	}
	/**
	 * return port of this server
	 * @return the port of this server
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * set port of this server
	 * @param port the port to set
	 */
	public void setPort(int port) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.port = port;
	}

	//--------------------------------------------------------------------------
	//
	protected void initNettyServer(){
		nettyServer=new ServerBootstrap();
		channelInitializer=new ProxyServerChannelInitializer();
		IOWorker worker=new IOWorker("ProxyServerIO",Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup = new NioEventLoopGroup(1,worker);
		workerGroup = new NioEventLoopGroup(0,worker);
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 128)    
		.option(ChannelOption.SO_REUSEADDR, true)    
		.option(ChannelOption.SO_RCVBUF, 1024*256)   
		.option(ChannelOption.SO_SNDBUF, 1024*256) 
		.childOption(ChannelOption.AUTO_READ, false)
		.childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true) 
		.childHandler(channelInitializer);
	}
	//
	class ProxyServerChannelInitializer extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			 ch.pipeline().addLast(
		               new ProxyFrontendHandler(ProxyServer.this)); 
		}
	}
	//--------------------------------------------------------------------------
	
	@Override
	public void init() throws Exception {
		initNettyServer();
	}
	//
	@Override
	public void start() throws Exception {
		nettyServer.bind(port).sync();
	}
	//
	@Override
	public void stop() throws Exception {
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	//--------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProxyServer server=new ProxyServer();
		server.addRule("bbs.tiexue.net",80);
		Jazmin.addServer(server);
		Jazmin.start();
	}
}
