/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class RelayChannel {
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
	public RelayChannel() {
		createTime=System.currentTimeMillis();
		lastAccessTime=createTime;
	}
	//
	void sendData2A(DatagramPacket pkg){
		if(remotePeerAddressA!=null){
			DatagramPacket dp=new DatagramPacket(
                    Unpooled.copiedBuffer(pkg.content()),
					remotePeerAddressA);
			channelA.writeAndFlush(dp);
		}
	}
	//
	void sendData2B(DatagramPacket pkg){
		if(remotePeerAddressB!=null){
			DatagramPacket dp=new DatagramPacket(
                    Unpooled.copiedBuffer(pkg.content()),
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
			sb.append(remotePeerAddressA.getHostString()+":"+remotePeerAddressA.getPort());
		}
		sb.append("-->");
		sb.append(localPeerPortA+"+"+localPeerPortB);
		sb.append("<--");
		if(remotePeerAddressB==null){
			sb.append("null");
		}else{
			sb.append(remotePeerAddressB.getHostString()+":"+remotePeerAddressB.getPort());
		}	
		return sb.toString();
	}
}
