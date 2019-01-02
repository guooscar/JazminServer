/**
 * 
 */
package jazmin.server.mysqlproxy;

import io.netty.channel.ChannelFuture;

/**
 *
 */
public class ProxyRule {
	//
	public String remoteHost;
	public int remotePort;
	public int localPort;
	public ProxyRuleAuthProvider authProvider;
	ChannelFuture channelFuture;
}
