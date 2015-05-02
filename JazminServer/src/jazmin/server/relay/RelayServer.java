/**
 * 
 */
package jazmin.server.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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

/**
 * relay UDP/TCP/SCTP package for NAT through
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
	public void addChannel(RelayChannel rc){
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
			switch (transportType) {
			case UDP:
				UDPRelayChannel rc=new UDPRelayChannel(hostAddress,nextPort);
				rc.outboundChannel=bindUDP(rc,nextPort);
				finalRelayChannel=rc;
				break;
			case TCP:
				SocketRelayChannel rc2=new SocketRelayChannel(hostAddress,nextPort);
				rc2.serverChannel=bindTCP(rc2,nextPort);
				finalRelayChannel=rc2;
				break;
			default:
				throw new IllegalArgumentException("unspport transport type:"
						+transportType);
			}
			portPool[nextPortIdx]=true;
			relayChannels.put(finalRelayChannel.id+"", finalRelayChannel);
			return finalRelayChannel;
		}
	}
	//
	private Channel bindUDP(UDPRelayChannel rc, int port) throws Exception {
		Bootstrap udpBootstrap=new Bootstrap();
		udpBootstrap.group(udpGroup).channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.handler(new RelayUDPChannelHandler(rc));
		logger.info("bind to udp {}:{}", hostAddress, port);
		return udpBootstrap.bind(hostAddress, port).sync().channel();
	}
	//
	private Channel bindTCP(SocketRelayChannel rc,int port) throws Exception {
		ServerBootstrap tcpBootstrap=new ServerBootstrap();
		tcpBootstrap.group(this.bossGroup, this.workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
            	ch.pipeline().addLast(new RelayTCPChannelHandler(rc));
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childOption(ChannelOption.TCP_NODELAY, true);
		logger.info("bind to tcp {}:{}", hostAddress, port);
		return tcpBootstrap.bind(port).sync().channel();
	}
	//
	private void checkIdleChannel(){
		long currentTime=System.currentTimeMillis();
		for(RelayChannel rc:relayChannels.values()){
			if(currentTime-rc.lastAccessTime>idleTime*1000){
				try{
					logger.info("remove idle channel."+rc);
					rc.close();
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
			cs.registerCommand(new RelayServerCommand());
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
