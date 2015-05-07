/**
 * 
 */
package jazmin.server.relay;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author yama
 *
 */
public class UDPRelayChannel extends NetworkRelayChannel{
	private  InetSocketAddress remoteAddress;
	
	UDPRelayChannel(RelayServer server,String localAddress, int localPort) {
		super(server,TransportType.UDP, localAddress, localPort);
	}
	//
	@Override
	public void dataFromPeer(InetSocketAddress remoteAddress, byte[] bytes)
			throws Exception {
		if(this.remoteAddress==null){
			this.remoteAddress=remoteAddress;
		}
		super.dataFromPeer(remoteAddress, bytes);
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte []buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		if(outboundChannel.isActive()){
			ByteBuf buf= Unpooled.wrappedBuffer(buffer);
			DatagramPacket dp=new DatagramPacket(
					buf,
					remoteAddress);
			outboundChannel.writeAndFlush(dp);
		}
	}

	/* 	 */
	@Override
	public String getInfo() {
		double bytePeerCnt=bytePeerCount;
		String networkInfo=packetPeerCount+"/"+String.format("%.2fKB",bytePeerCnt/1024);
		String remoteAddressStr="";
		if(remoteAddress!=null){
			remoteAddressStr=remoteAddress.getAddress().getHostAddress()
					+":"+remoteAddress.getPort();
		}
		return transportType+"["+localHostAddress+":"+localPort+"<-->"+remoteAddressStr+"] "+networkInfo;
	}

}
