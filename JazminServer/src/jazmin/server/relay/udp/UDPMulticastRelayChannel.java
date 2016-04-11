/**
 * 
 */
package jazmin.server.relay.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

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
public class UDPMulticastRelayChannel extends NetworkRelayChannel{
	private static Logger logger=LoggerFactory.get(UDPMulticastRelayChannel.class);
	private long idleTime;
	Channel outboundChannel;
	//
	class PeerConnection{
		String id;
		InetSocketAddress address;
		long lastAccessTime;
		int pkgSentCount;
		int pkgReceiveCount;
	}
	//
	Map<String,PeerConnection> peerConnectionMap;
	//
	public UDPMulticastRelayChannel(RelayServer server,String localAddress, int localPort) {
		super(server,TransportType.UDP_MULTICAST, localAddress, localPort);
		peerConnectionMap=new ConcurrentHashMap<String, UDPMulticastRelayChannel.PeerConnection>();
		idleTime=60*1000;
	}
	//
	@Override
	public void dataFromPeer(InetSocketAddress remoteAddress, byte[] bytes)
			throws Exception {
		String id=remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort();
		PeerConnection peerConnection=peerConnectionMap.get(id);
		if(peerConnection==null){
			peerConnection=new PeerConnection();
			peerConnection.id=id;
			peerConnection.address=remoteAddress;
			peerConnectionMap.put(id, peerConnection);
		}
		peerConnection.lastAccessTime=System.currentTimeMillis();
		peerConnection.pkgReceiveCount++;
		super.dataFromPeer(remoteAddress, bytes);
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte []buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		if(outboundChannel.isActive()){
			for(PeerConnection pc:peerConnectionMap.values()){
				ByteBuf buf= Unpooled.wrappedBuffer(buffer);
				DatagramPacket dp=new DatagramPacket(
						buf,
						pc.address);
				pc.pkgSentCount++;
				outboundChannel.writeAndFlush(dp);		
			}
		}
	}
	//
	public void checkStatus() {
		long now=System.currentTimeMillis();
		for(PeerConnection pc:peerConnectionMap.values()){
			if((now-pc.lastAccessTime)>idleTime){
				peerConnectionMap.remove(pc.id);
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
