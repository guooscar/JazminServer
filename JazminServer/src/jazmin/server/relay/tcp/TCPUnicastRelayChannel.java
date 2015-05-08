/**
 * 
 */
package jazmin.server.relay.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import jazmin.server.relay.NetworkRelayChannel;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;

/**
 * @author yama
 *
 */
public class TCPUnicastRelayChannel extends NetworkRelayChannel{
	InetSocketAddress remoteAddress;
	Channel outboundChannel;
	//
	public TCPUnicastRelayChannel(RelayServer server,String localAddress, int localPort) {
		super(server,TransportType.TCP_UNICAST,localAddress, localPort);
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
