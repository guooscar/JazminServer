/**
 * 
 */
package jazmin.server.sip;

import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.stack.Connection;
import jazmin.util.DumpIgnore;

/**
 * @author yama
 *
 */
@DumpIgnore
public class SipContext {
	Connection connection;
	SipMessage message;
	SipServer server;
	
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	/**
	 * @return the message
	 */
	public SipMessage getMessage() {
		return message;
	}
	/**
	 * @return the server
	 */
	public SipServer getServer() {
		return server;
	}
	/**
	 * 
	 */
	public SipSession getSession(){
		return server.getSession(message,true);
	}
	/**
	 * 
	 */
	public SipSession getSession(boolean create){
		return server.getSession(message,create);
	}
}
