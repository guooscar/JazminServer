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
	public void dataFromRelay(RelayChannel channel,byte []buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		if(outboundChannel!=null&&outboundChannel.isActive()){
			outboundChannel.writeAndFlush(Unpooled.wrappedBuffer(buffer));
		}
	}
}
