/**
 * 
 */
package jazmin.server.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;

/**
 * relay UDP package for NAT through
 * @author yama 26 Apr, 2015
 */
public class RelayServer extends Server{
	private static Logger logger=LoggerFactory.get(RelayServer.class);
	//
	private EventLoopGroup group;
	private List<RelayChannel>relayChannels;
	private int idleTime;
	private int minStartPort;
	private int maxStartPort;
	private String hostAddress;
	private List<String>hostAddresses;
	//
	private boolean portPool[];
	//
	public RelayServer() {
		hostAddress="0.0.0.0";
		minStartPort=10000;
		maxStartPort=30000;
		idleTime=30;//30sec
		IOWorker ioWorker=new IOWorker("RelayIOWorker",
    			Runtime.getRuntime().availableProcessors()*2+1);
		group = new NioEventLoopGroup(0,ioWorker);
		relayChannels=Collections.synchronizedList(new LinkedList<RelayChannel>());
		Jazmin.scheduleAtFixedRate(this::checkIdleChannel,idleTime/2,idleTime/2, 
				TimeUnit.SECONDS);
		portPool=new boolean[maxStartPort-minStartPort];
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
	 * @param hostAddresses the hostAddresses to set
	 */
	public void addHostAddress(String addr) {
		this.hostAddresses.add(addr);
	}

	//
	public List<RelayChannel>getChannels(){
		return new ArrayList<RelayChannel>(relayChannels);
	}
	//
	public RelayChannel createRelayChannel(String name) throws Exception {
		RelayChannel rc=createRelayChannel();
		rc.setName(name);
		return rc;
	}
	//
	public RelayChannel createRelayChannel() throws Exception {
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
			//
			portPool[nextPortIdx]=true;
			return createRelayChannel(minStartPort+nextPortIdx, maxStartPort+nextPortIdx);
		}
	}
	//
	private RelayChannel createRelayChannel(int portA,int portB) throws Exception {
		RelayChannel rc = new RelayChannel();
		rc.localHostAddress=hostAddress;
		rc.localPeerPortA = portA;
		rc.localPeerPortB = portB;
		// binding port
		rc.channelA=bind(rc,rc.localPeerPortA);
		rc.channelB=bind(rc,rc.localPeerPortB);
		//
		relayChannels.add(rc);
		logger.info("create channel:"+rc);
		return rc;
	}
	//
	private void checkIdleChannel(){
		Iterator<RelayChannel>it=relayChannels.iterator();
		long currentTime=System.currentTimeMillis();
		while(it.hasNext()){
			RelayChannel channel=it.next();
			if(currentTime-channel.lastAccessTime>idleTime*1000){
				try{
					logger.info("remove idle channel."+channel);
					channel.close();
					relayChannelClosed(channel);
				}catch(Exception e){
					logger.catching(e);
				}
				it.remove();
			}
		}
	}
	//
	void relayChannelClosed(RelayChannel channel){
		synchronized (channel) {
			portPool[channel.localPeerPortA-minStartPort]=false;
		}
	}
	//
	private Channel bind(RelayChannel rc,int port)throws Exception{
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioDatagramChannel.class)
			.option(ChannelOption.SO_BROADCAST, true)
			.handler(new RelayChannelHandler(rc,port));
		return b.bind(hostAddress,port).sync().channel();
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
	public int getMinStartPort() {
		return minStartPort;
	}
	/**
	 * @param minStartPort the minStartPort to set
	 */
	public void setMinStartPort(int minStartPort) {
		this.minStartPort = minStartPort;
	}
	/**
	 * @return the maxStartPort
	 */
	public int getMaxStartPort() {
		return maxStartPort;
	}
	/**
	 * @param maxStartPort the maxStartPort to set
	 */
	public void setMaxStartPort(int maxStartPort) {
		this.maxStartPort = maxStartPort;
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
	public String info() {
		InfoBuilder ib = InfoBuilder.create();
		ib.section("info").format("%-30s:%-30s\n")
				.print("minStartPort", getMinStartPort())
				.print("maxStartPort", getMaxStartPort())
				.print("idleTime", getIdleTime())
				.print("hostAddress", getHostAddress());
		return ib.toString();
	}
}
