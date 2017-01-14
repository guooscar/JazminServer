package jazmin.server.msg;

import java.net.InetSocketAddress;

/**
 * 
 * @author yama
 *
 */
public interface NetworkChannel {
	InetSocketAddress getRemoteAddress();
	void close();
	void writeAndFlush(Object obj);
	
}
