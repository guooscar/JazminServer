/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class RelayChannel {
	String localHostAddress;
	//
	int localPeerPortA;
	int localPeerPortB;
	
	InetSocketAddress remotePeerAddressA;
	InetSocketAddress remotePeerAddressB;
	//
	Channel channelA;
	Channel channelB;
	//
	long createTime;
	long lastAccessTime;
	//
	long peerAPacketCount;
	long peerBPacketCount;
	long peerAByteCount;
	long peerBByteCount;
	//
	//
	public RelayChannel() {
		createTime=System.currentTimeMillis();
		lastAccessTime=createTime;
	}
	//--------------------------------------------------------------------------
	/**
	 * @return the localPeerPortA
	 */
	public int getLocalPeerPortA() {
		return localPeerPortA;
	}

	/**
	 * @return the localPeerPortB
	 */
	public int getLocalPeerPortB() {
		return localPeerPortB;
	}

	/**
	 * @return the createTime
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * @return the lastAccessTime
	 */
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	
	/**
	 * @return the localHostAddress
	 */
	public String getLocalHostAddress() {
		return localHostAddress;
	}
	/**
	 * @return the remotePeerAddressA
	 */
	public InetSocketAddress getRemotePeerAddressA() {
		return remotePeerAddressA;
	}
	/**
	 * @return the remotePeerAddressB
	 */
	public InetSocketAddress getRemotePeerAddressB() {
		return remotePeerAddressB;
	}
	//--------------------------------------------------------------------------
	void sendData2A(DatagramPacket pkg){
		if(remotePeerAddressA!=null){
			ByteBuf buf= Unpooled.copiedBuffer(pkg.content());
			peerBByteCount+=buf.capacity();
			peerBPacketCount++;
			DatagramPacket dp=new DatagramPacket(
					buf,
					remotePeerAddressA);
			channelA.writeAndFlush(dp);
		}
	}
	//
	void sendData2B(DatagramPacket pkg){
		if(remotePeerAddressB!=null){
			ByteBuf buf= Unpooled.copiedBuffer(pkg.content());
			peerAByteCount+=buf.capacity();
			peerAPacketCount++;
			DatagramPacket dp=new DatagramPacket(
					buf ,
					remotePeerAddressB);
			channelB.writeAndFlush(dp);
		}
	}
	//
	void close(){
		if(channelA!=null){
			channelA.close();
		}
		if(channelB!=null){
			channelB.close();
		}
	}
	//
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		if(remotePeerAddressA==null){
			sb.append("null");
		}else{
			sb.append(remotePeerAddressA.getAddress().getHostAddress()+":"+remotePeerAddressA.getPort());
		}
		sb.append("-->");
		sb.append(localPeerPortA+"+"+localPeerPortB);
		sb.append("<--");
		if(remotePeerAddressB==null){
			sb.append("null");
		}else{
			sb.append(remotePeerAddressB.getAddress().getHostAddress()+":"+remotePeerAddressB.getPort());
		}	
		return sb.toString();
	}
}
