/**
 * 
 */
package jazmin.server.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.tcp.TCPMulticastRelayChannel;
import jazmin.server.relay.tcp.TCPMulticastRelayChannelHandler;
import jazmin.server.relay.tcp.TCPUnicastRelayChannel;
import jazmin.server.relay.tcp.TCPUnicastRelayChannelHandler;
import jazmin.server.relay.udp.DtlsRelayChannel;
import jazmin.server.relay.udp.DtlsRelayChannelHandler;
import jazmin.server.relay.udp.UDPMulticastRelayChannel;
import jazmin.server.relay.udp.UDPRelayChannelHandler;
import jazmin.server.relay.udp.UDPUnicastRelayChannel;

/**
 * relay UDP/TCP/DTLS-SRTP package for NAT through
 * @author yama 26 Apr, 2015
 */
public class RelayServer extends Server{
	private static Logger logger=LoggerFactory.get(RelayServer.class);
	//
	private Map<String,RelayChannel>relayChannels;
	private int idleTime;
	private int minBindPort;
	private int maxBindPort;
	private String hostAddress;
	private List<String>hostAddresses;
	//
	private EventLoopGroup udpGroup;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	//
	private boolean portPool[];
	//
	public RelayServer() {
		hostAddress="0.0.0.0";
		minBindPort=10000;
		maxBindPort=65535;
		idleTime=30;//30sec
		relayChannels=new ConcurrentHashMap<String, RelayChannel>();
		
		portPool=new boolean[maxBindPort-minBindPort];
		Arrays.fill(portPool, false);
		hostAddresses=new ArrayList<>();
	}
	/**
	 * @return the hostAddresses
	 */
	public List<String> getHostAddresses() {
		return hostAddresses;
	}
	/**
	 * add channel to server
	 * @param rc
	 */
	void addChannel(RelayChannel rc){
		if(relayChannels.containsKey(rc.id+"")){
			throw new IllegalArgumentException("channel # "+rc.id+"already exists");
		}
		relayChannels.put(rc.id+"",rc);
	}
	/**
	 * @param hostAddresses the hostAddresses to set
	 */
	public void addHostAddress(String addr) {
		this.hostAddresses.add(addr);
	}
	//
	public List<RelayChannel>getChannels(){
		return new ArrayList<RelayChannel>(relayChannels.values());
	}
	//
	public RelayChannel getChannel(String id){
		return relayChannels.get(id);
	}
	//
	private void freePort(int port){
		synchronized (portPool) {
			portPool[port-minBindPort]=false;
		}
	}
	/**
	 * create a network relay channel using specified transport type and name
	 * @param transportType the network transport type
	 * @param name the relay channel name
	 * @return relay channel
	 * @throws Exception
	 */
	public NetworkRelayChannel createRelayChannel(TransportType transportType,String name) 
			throws Exception {
		NetworkRelayChannel rc=createRelayChannel(transportType);
		rc.setName(name);
		return rc;
	}
	/**
	 * create relay channel using specified transport type
	 * @param transportType the relay channel transport type 
	 * @return
	 * @throws Exception
	 */
	public NetworkRelayChannel createRelayChannel(TransportType transportType)
			throws Exception{
		for(int i=0;i<10;i++){
			NetworkRelayChannel rc=createRelayChannel0(transportType);
			if(rc!=null){
				return rc;
			}
		}
		throw new IllegalStateException("can not assign port");
	}
	//
	private NetworkRelayChannel createRelayChannel0(TransportType transportType) 
			throws Exception {
		synchronized (portPool) {
			int nextPortIdx=-1;
			for(int i=0;i<portPool.length;i++){
				if(!portPool[i]){
					nextPortIdx=i;
					break;
				}
			}
			if(nextPortIdx==-1){
				throw new IllegalStateException("all port in use");
			}
			int nextPort= nextPortIdx+minBindPort;
			NetworkRelayChannel finalRelayChannel=null;
			try{
				finalRelayChannel=bindPort(transportType, nextPort);
			}catch(BindException e){
				logger.warn("port {} already in use",nextPort);
			}finally{
				portPool[nextPortIdx]=true;		
			}
			return finalRelayChannel;
		}
	}
	//
	private NetworkRelayChannel bindPort(TransportType transportType,int nextPort) throws Exception{
		NetworkRelayChannel finalRelayChannel=null;
		switch (transportType) {
		case UDP_UNICAST:
			UDPUnicastRelayChannel rc=new UDPUnicastRelayChannel(this,hostAddress,nextPort);
			rc.setServerChannel(bindUDP(rc,nextPort));
			finalRelayChannel=rc;
			break;
		case UDP_MULTICAST:
			UDPMulticastRelayChannel urc=new UDPMulticastRelayChannel(this,hostAddress,nextPort);
			urc.setServerChannel(bindUDP(urc,nextPort));
			finalRelayChannel=urc;
			break;
		case TCP_UNICAST:
			TCPUnicastRelayChannel rc2=new TCPUnicastRelayChannel(this,hostAddress,nextPort);
			rc2.setServerChannel(bindTCP(rc2,nextPort));
			finalRelayChannel=rc2;
			break;
		case TCP_MULTICAST:
			TCPMulticastRelayChannel tmrc=new TCPMulticastRelayChannel(this,hostAddress,nextPort);
			tmrc.setServerChannel(bindTCP(tmrc,nextPort));
			finalRelayChannel=tmrc;
			break;
		case DTLS:
			DtlsRelayChannel rc3=new DtlsRelayChannel(this,hostAddress,nextPort);
			rc3.setServerChannel(bindDtls(rc3,nextPort));
			finalRelayChannel=rc3;
			break;
		default:
			throw new IllegalArgumentException("unspport transport type:"
					+transportType);
		}
		return finalRelayChannel;
	}
	//
	private Channel bindDtls(DtlsRelayChannel rc, int port) throws Exception {
		Bootstrap udpBootstrap=new Bootstrap();
		udpBootstrap.group(udpGroup).channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.handler(new DtlsRelayChannelHandler(rc));
		logger.info("bind to dtls {}:{}", hostAddress, port);
		return udpBootstrap.bind(hostAddress, port).sync().channel();
	}
	//
	private Channel bindUDP(NetworkRelayChannel rc, int port) throws Exception {
		Bootstrap udpBootstrap=new Bootstrap();
		udpBootstrap.group(udpGroup).channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.handler(new UDPRelayChannelHandler(rc));
		logger.info("bind to udp {}:{}", hostAddress, port);
		return udpBootstrap.bind(hostAddress, port).sync().channel();
	}
	//
	private Channel bindTCP(NetworkRelayChannel rc,int port,ChannelHandler handler) throws Exception {
		ServerBootstrap tcpBootstrap=new ServerBootstrap();
		tcpBootstrap.group(this.bossGroup, this.workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
            	ch.pipeline().addLast(handler);
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childOption(ChannelOption.TCP_NODELAY, true);
		logger.info("bind to tcp {}:{}", hostAddress, port);
		return tcpBootstrap.bind(port).sync().channel();
	}
	//
	private Channel bindTCP(NetworkRelayChannel rc,int port) throws Exception {
		if(rc instanceof TCPUnicastRelayChannel){
			return bindTCP(rc, port, new TCPUnicastRelayChannelHandler((TCPUnicastRelayChannel)rc));			
		}
		if(rc instanceof TCPMulticastRelayChannel){
			return bindTCP(rc, port, new TCPMulticastRelayChannelHandler((TCPMulticastRelayChannel)rc));			
		}
		throw new IllegalArgumentException("not support");
	}
	//
	private void checkIdleChannel(){
		long currentTime=System.currentTimeMillis();
		for(RelayChannel rc:relayChannels.values()){
			rc.checkStatus();
			if(currentTime-rc.lastAccessTime>idleTime*1000){
				try{
					logger.info("remove idle channel."+rc);
					rc.closeChannel();
					if(rc instanceof NetworkRelayChannel){
						freePort(((NetworkRelayChannel)rc).localPort);		
					}
					relayChannels.remove(rc.id);
				}catch(Exception e){
					logger.catching(e);
				}
			}
		}
	}
	//--------------------------------------------------------------------------
	/**
	 * @return the idleTime
	 */
	public int getIdleTime() {
		return idleTime;
	}
	/**
	 * @param idleTime the idleTime to set
	 */
	public void setIdleTime(int idleTime) {
		this.idleTime = idleTime;
	}
	
	/**
	 * @return the minStartPort
	 */
	public int getMinBindPort() {
		return minBindPort;
	}
	/**
	 * @param minStartPort the minStartPort to set
	 */
	public void setMinBindPort(int minStartPort) {
		this.minBindPort = minStartPort;
	}
	/**
	 * @return the maxStartPort
	 */
	public int getMaxBindPort() {
		return maxBindPort;
	}
	/**
	 * @param maxStartPort the maxStartPort to set
	 */
	public void setMaxBindPort(int maxStartPort) {
		this.maxBindPort = maxStartPort;
	}
	/**
	 * @return the hostAddress
	 */
	public String getHostAddress() {
		return hostAddress;
	}
	/**
	 * @param hostAddress the hostAddress to set
	 */
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(RelayServerCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		IOWorker ioWorker=new IOWorker("RelayIOWorker",
    			Runtime.getRuntime().availableProcessors()*2+1);
		Jazmin.scheduleAtFixedRate(this::checkIdleChannel,idleTime/2,idleTime/2, 
				TimeUnit.SECONDS);
		udpGroup = new NioEventLoopGroup(0,ioWorker);
		bossGroup=new NioEventLoopGroup(1,ioWorker);
    	workerGroup=new NioEventLoopGroup(0,ioWorker);
	}
	//
	@Override
	public void stop() throws Exception {
		if(udpGroup!=null){
			udpGroup.shutdownGracefully();
		}
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
		}
		if(workerGroup!=null){
			workerGroup.shutdownGracefully();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib = InfoBuilder.create();
		ib.section("info").format("%-30s:%-30s\n")
				.print("minBindPort", getMinBindPort())
				.print("maxBindPort", getMaxBindPort())
				.print("idleTime", getIdleTime())
				.print("hostAddress", getHostAddress())
				.print("hostAddresses", getHostAddresses());
		return ib.toString();
	}
}
