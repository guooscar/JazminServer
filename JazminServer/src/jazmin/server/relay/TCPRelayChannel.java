/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author yama
 *
 */
public class TCPRelayChannel extends NetworkRelayChannel{
	Channel serverChannel;
	InetSocketAddress remoteAddress;
	//
	TCPRelayChannel(RelayServer server,String localAddress, int localPort) {
		super(server,TransportType.TCP , localAddress, localPort);
	}
	//
	@Override
	public void closeChannel() throws Exception {
		super.closeChannel();
		if(serverChannel!=null){
			serverChannel.close();
		}
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte []buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		if(outboundChannel!=null&&outboundChannel.isActive()){
			outboundChannel.writeAndFlush(Unpooled.wrappedBuffer(buffer));
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
