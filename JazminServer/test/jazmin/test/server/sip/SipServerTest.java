/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.SipServer;

/**
 * @author g2131
 *
 */
public class SipServerTest {

	//
	//--------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		LoggerFactory.setLevel("DEBUG");
		SipServer server=new SipServer();
		server.setHostAddress("192.168.3.103");
		server.setMessageHandler(new B2BUAMessageHandler());
		Jazmin.addServer(server);
		Jazmin.start();
	}
}
