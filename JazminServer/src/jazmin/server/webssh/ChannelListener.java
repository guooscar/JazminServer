/**
 * 
 */
package jazmin.server.webssh;

/**
 * @author yama
 *
 */
public interface ChannelListener {
	void onOpen(WebSshChannel channel);
	void onClose(WebSshChannel channel);
	void onMessage(WebSshChannel channel,String message);
	boolean onInput(WebSshChannel channel,String message);
	void onTicket(WebSshChannel channel,long ticket);
}
