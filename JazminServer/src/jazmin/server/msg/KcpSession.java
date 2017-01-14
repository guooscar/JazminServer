/**
 * 
 */
package jazmin.server.msg;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class KcpSession extends Session{
	public KcpSession(NetworkChannel channel) {
		super(channel);
		connectionType="kcp";
	}
}
