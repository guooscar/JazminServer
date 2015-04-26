/**
 * 
 */
package jazmin.server.relay;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class RelayChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	//
	RelayChannel relayChannel;
	int localPort;
	public RelayChannelHandler(RelayChannel relayChannel,int localPort) {
		this.relayChannel=relayChannel;
		this.localPort=localPort;
	}
	//
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			DatagramPacket pkg) throws Exception {
		InetSocketAddress isa=pkg.sender();
		relayChannel.lastAccessTime=System.currentTimeMillis();
		if(localPort==relayChannel.localPeerPortA){
			if(relayChannel.remotePeerAddressA==null){
				relayChannel.remotePeerAddressA=new InetSocketAddress(
						isa.getHostName(),isa.getPort());
			}
			relayChannel.sendData2B(pkg);
		
		}else{
			if(relayChannel.remotePeerAddressB==null){
				relayChannel.remotePeerAddressB=new InetSocketAddress(
						isa.getHostName(),isa.getPort());
			}
			relayChannel.sendData2A(pkg);
		}
	}
	
}
