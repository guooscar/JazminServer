/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * @author yama
 *
 */
public class SocketRelayChannel extends NetworkRelayChannel{
	Channel serverChannel;
	//
	SocketRelayChannel(String localAddress, int localPort) {
		super(TransportType.TCP , localAddress, localPort);
	}
	//
	@Override
	public void close() throws Exception {
		super.close();
		if(serverChannel!=null){
			serverChannel.close();
		}
	}
	//
	@Override
	public void write(ByteBuf buffer) {
		if(outboundChannel!=null&&outboundChannel.isActive()){
			ByteBuf buf= Unpooled.copiedBuffer(buffer);
			packetSentCount++;
			byteSentCount+=buffer.capacity();
			outboundChannel.writeAndFlush(buf);
		}
	}
}
