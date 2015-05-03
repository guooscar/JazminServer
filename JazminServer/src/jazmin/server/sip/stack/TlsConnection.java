/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author yama
 * 3 May, 2015
 */
public class TlsConnection extends TcpConnection{

	public TlsConnection(Channel channel, InetSocketAddress remote) {
		super(channel, remote);
	}
	@Override
	public boolean isTCP() {
		return false;
	}
	@Override
	public boolean isTLS() {
		return true;
	}
}
