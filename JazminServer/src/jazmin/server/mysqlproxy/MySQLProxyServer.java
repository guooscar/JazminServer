/**
 * 
 */
package jazmin.server.mysqlproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class MySQLProxyServer extends Server{
	private static Logger logger=LoggerFactory.get(MySQLProxyServer.class);
	//
	ServerBootstrap nettyServer;
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	IOWorker worker;
	ConcurrentMap<String,ProxySession>sessionMap;
	ConcurrentMap<Integer,ProxyRule>ruleMap;
	//
	public MySQLProxyServer() {
		ruleMap=new ConcurrentHashMap<Integer, ProxyRule>();
		sessionMap=new ConcurrentHashMap<String, ProxySession>();
		worker=new IOWorker("MySQLProxyServerIO",Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup = new NioEventLoopGroup(1,worker);
		workerGroup = new NioEventLoopGroup(0,worker);
	}
	//
	public List<ProxySession>getSessions(){
		return new ArrayList<>(sessionMap.values());
	}
	//
	public void addSession(ProxySession session){
		sessionMap.put(session.id, session);
	}
	//
	public void removeSession(String id){
		sessionMap.remove(id);
	}
	//
	public List<ProxyRule>getRules(){
		return new ArrayList<ProxyRule>(ruleMap.values());
	}
	//
	public void addRule(ProxyRule rule) {
		if(ruleMap.containsKey(rule.localPort)){
			throw new  IllegalArgumentException("local port:"+rule.localPort+" already binded");
		}
		ruleMap.put(rule.localPort, rule);
		initNettyServer(rule);
	}
	//
	public void removeRule(int port){
		ProxyRule rule=ruleMap.remove(port);
		if(rule!=null){
			try {
				rule.channelFuture.channel().close().sync();
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}
	//--------------------------------------------------------------------------
	//
	protected void initNettyServer(ProxyRule rule){
		nettyServer=new ServerBootstrap();
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 128)    
		.option(ChannelOption.SO_REUSEADDR, true)    
		.option(ChannelOption.SO_RCVBUF, 1024*256)   
		.option(ChannelOption.SO_SNDBUF, 1024*256) 
		.childOption(ChannelOption.AUTO_READ, false)
		.childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true) 
		.childHandler(new ProxyServerChannelInitializer(this,rule));
		ChannelFuture channelFuture;
		try {
			logger.info("listen port :"+rule.localPort+" for remote server "+rule.remoteHost+":"+rule.remotePort);
			channelFuture = nettyServer.bind(rule.localPort).sync();
			rule.channelFuture=channelFuture;
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	//
	public static class ProxyServerChannelInitializer extends ChannelInitializer<SocketChannel>{
		ProxyRule rule;
		MySQLProxyServer server;
		ProxyServerChannelInitializer(MySQLProxyServer server,ProxyRule rule){
			this.rule=rule;
			this.server=server;
		}
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			 ch.pipeline().addLast(
					 	new PacketDecoder(),
		                new ProxyFrontendHandler(server,rule)); 
		}
	}
	//
	public static class ProxyServerBackendChannelInitializer extends ChannelInitializer<SocketChannel>{
		Channel inChannel;
		MySQLProxyServer server;
		ProxyRule rule;
		ProxyFrontendHandler frontendHander;
		ProxyServerBackendChannelInitializer(MySQLProxyServer server,ProxyRule rule,ProxyFrontendHandler frontendHander,Channel inChannel){
			this.inChannel=inChannel;
			this.server=server;
			this.rule=rule;
			this.frontendHander=frontendHander;
		}
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			 ch.pipeline().addLast(
					 	new PacketDecoder(),
		                new ProxyBackendHandler(server,rule,frontendHander,inChannel)); 
		}
	}
	//--------------------------------------------------------------------------
	
	@Override
	public void init() throws Exception {
		ConsoleServer cs = Jazmin.getServer(ConsoleServer.class);
		if (cs != null) {
			cs.registerCommand(MySQLProxyServerCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		
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
}
