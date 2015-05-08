/**
 * 
 */
package jazmin.server.relay.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.relay.NetworkRelayChannel;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.util.DumpUtil;

/**
 * @author yama
 *
 */
public class TCPMulticastRelayChannel extends NetworkRelayChannel{
	private static Logger logger=LoggerFactory.get(TCPMulticastRelayChannel.class);
	private long idleTime;
		//
	class PeerConnection{
		String id;
		InetSocketAddress address;
		Channel peerChannel;
		long lastAccessTime;
		int pkgSentCount;
		int pkgReceiveCount;
	}
	//
	Map<String,PeerConnection> peerConnectionMap;
	//
	public TCPMulticastRelayChannel(RelayServer server,String localAddress, int localPort) {
		super(server,TransportType.TCP_MULTICAST, localAddress, localPort);
		peerConnectionMap=new ConcurrentHashMap<String, TCPMulticastRelayChannel.PeerConnection>();
		idleTime=60*1000;
	}
	//
	public void dataFromPeer(InetSocketAddress remoteAddress,Channel peerChannel, byte[] bytes)
			throws Exception {
		String id=remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort();
		PeerConnection peerConnection=peerConnectionMap.get(id);
		
		peerConnection.lastAccessTime=System.currentTimeMillis();
		peerConnection.pkgReceiveCount++;
		super.dataFromPeer(remoteAddress, bytes);
	}
	//
	void peerConnectionInactive(Channel peerChannel){
		InetSocketAddress remoteAddress=(InetSocketAddress) peerChannel.remoteAddress();
		String id=remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort();
		peerConnectionMap.remove(id);
	}
	//
	void peerConnectionActive(Channel peerChannel){
		InetSocketAddress remoteAddress=(InetSocketAddress) peerChannel.remoteAddress();
		String id=remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort();
		PeerConnection	peerConnection=new PeerConnection();
		peerConnection.id=id;
		peerConnection.peerChannel=peerChannel;
		peerConnection.address=remoteAddress;
		peerConnectionMap.put(id, peerConnection);
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte []buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		for(PeerConnection pc:peerConnectionMap.values()){
			if(pc.peerChannel.isActive()){
				ByteBuf buf= Unpooled.wrappedBuffer(buffer);
				pc.peerChannel.writeAndFlush(buf);
			}else{
				peerConnectionMap.remove(pc.id);
			}
		}
		
	}
	//
	public void checkStatus() {
		long now=System.currentTimeMillis();
		for(PeerConnection pc:peerConnectionMap.values()){
			if((now-pc.lastAccessTime)>idleTime){
				peerConnectionMap.remove(pc.id);
				pc.peerChannel.close();
				logger.info("remove idle peer connection:"+pc.id);
			}
		}
	}
	//
	/* 	 */
	@Override
	public String getInfo() {
		String networkInfo=packetPeerCount+"/"+DumpUtil.byteCountToString(bytePeerCount);
		StringBuilder sb=new StringBuilder();
		sb.append(transportType+"["+localHostAddress+":"+localPort+"<-->]"+networkInfo+"\n");
		SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(PeerConnection pc:peerConnectionMap.values()){
			sb.append(pc.id+"\t lastAccess:"+sdf.format(new Date(pc.lastAccessTime)))
			.append("\tpkgSent:"+pc.pkgSentCount)
			.append("\tpkgReceive:"+pc.pkgReceiveCount)
			.append("\n");
		}
		return sb.toString();
	}

}
