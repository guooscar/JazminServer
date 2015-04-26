/**
 * 
 */
package jazmin.server.relay;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * relay UDP package for NAT through
 * @author yama 26 Apr, 2015
 */
public class RelayServer {
	private static Logger logger=LoggerFactory.get(RelayServer.class);
	//
	private EventLoopGroup group;
	private List<RelayChannel>relayChannels;
	private int idleTime;
	private int minPort;
	private int maxPort;
	//
	private boolean portPool[];
	public RelayServer() {
		minPort=10000;
		maxPort=60000;
		idleTime=30;//30sec
		group = new NioEventLoopGroup();
		relayChannels=Collections.synchronizedList(new LinkedList<RelayChannel>());
		Jazmin.scheduleAtFixedRate(this::checkIdleChannel,idleTime/2,idleTime/2, 
				TimeUnit.SECONDS);
		portPool=new boolean[maxPort-minPort];
		Arrays.fill(portPool, false);
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
			portPool[nextPortIdx+1]=true;
			return createRelayChannel(minPort+nextPortIdx, minPort+nextPortIdx+1);
		}
	}
	//
	private RelayChannel createRelayChannel(int portA,int portB) throws Exception {
		RelayChannel rc = new RelayChannel();
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
					relayChannelClosed(channel);
					channel.close();
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
			portPool[channel.localPeerPortA-minPort]=false;
			portPool[channel.localPeerPortB-minPort]=false;
		}
	}
	//
	private Channel bind(RelayChannel rc,int port)throws Exception{
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioDatagramChannel.class)
			.option(ChannelOption.SO_BROADCAST, true)
			.handler(new RelayChannelHandler(rc,port));
		return b.bind(port).sync().channel();
	}
	//
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		Jazmin.start();
		RelayServer server=new RelayServer();
		server.createRelayChannel();
	}

}
