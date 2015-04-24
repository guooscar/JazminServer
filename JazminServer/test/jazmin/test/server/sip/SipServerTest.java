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
		server.setIp("10.44.218.63");
		server.setMessageHandler(new TestSipMessageHandler());
		Jazmin.addServer(server);
		Jazmin.start();
	}
}
