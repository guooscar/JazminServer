/**
 * 
 */
package jazmin.server.sip;

import io.netty.util.AttributeKey;

import java.util.Date;


/**
 * @author yama
 *
 */
public class SipChannel {
	public static final AttributeKey<SipChannel> SESSION_KEY=
			AttributeKey.valueOf("s");
	
	public String transport;
	public String id;
	public String localAddress;
	public int localPort;
	public String remoteAddress;
	public int remotePort;
	public Date createTime;
	public long messageReceivedCount=0;
	public long messageSentCount=0;
	public SipChannel() {
		createTime=new Date();
	}
}
