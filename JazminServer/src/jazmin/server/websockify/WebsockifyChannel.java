/**
 * 
 */
package jazmin.server.websockify;

import java.util.Date;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;


/**
 * @author yama
 *
 */
public class WebsockifyChannel {
	//
	public static final AttributeKey<WebsockifyChannel> SESSION_KEY=
			AttributeKey.valueOf("s");
	
	public String id;
	public String remoteAddress;
	public int remotePort;
	public Date createTime;
	public long messageReceivedCount=0;
	public long messageSentCount=0;
	public Channel inBoundChannel;
	public Channel outBoundChannel;
	//
	public WebsockifyChannel() {
		createTime=new Date();
	}
}
