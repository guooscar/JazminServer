/**
 * 
 */
package jazmin.server.msg;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.channel.Channel;

/**
 * @author yama
 *
 */
public class NettyNetworkChannel implements NetworkChannel{
	private Channel channel;
	public NettyNetworkChannel(Channel chanel) {
		this.channel=chanel;
	}
	//
	@Override
	public InetSocketAddress getRemoteAddress() {
		SocketAddress sa=channel.remoteAddress();
		return sa==null?null:(InetSocketAddress)sa ;
	}
	//
	@Override
	public void close() {
		try {
			channel.close().sync();
		} catch (Exception e) {
		}	
	}
	//
	@Override
	public void writeAndFlush(Object obj) {
		channel.writeAndFlush(obj);
	}
	
}
