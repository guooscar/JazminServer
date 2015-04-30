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
	void sendData(ByteBuf buffer) {
		if(outboundChannel.isActive()){
			ByteBuf buf= Unpooled.copiedBuffer(buffer);
			DatagramPacket dp=new DatagramPacket(
					buf,
					remoteAddress);
			packetSentCount++;
			byteSentCount+=buffer.capacity();
			outboundChannel.writeAndFlush(dp);
		}
	}
}
