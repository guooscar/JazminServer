/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * @author yama
 *
 */
public class TCPRelayChannel extends NetworkRelayChannel{
	Channel serverChannel;
	//
	TCPRelayChannel(String localAddress, int localPort) {
		super(TransportType.TCP , localAddress, localPort);
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
	public void write(byte []buffer) {
		if(outboundChannel!=null&&outboundChannel.isActive()){
			packetSentCount++;
			byteSentCount+=buffer.length;
			outboundChannel.writeAndFlush(Unpooled.wrappedBuffer(buffer));
		}
	}
}
