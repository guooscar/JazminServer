/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author yama
 *
 */
public class UDPRelayChannel extends NetworkRelayChannel{

	UDPRelayChannel(String localAddress, int localPort) {
		super(TransportType.UDP, localAddress, localPort);
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
}
