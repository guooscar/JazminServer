/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.RelayServer;
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
		server.setHostAddress("10.44.218.63");
		server.setMessageHandler(new B2BUAMessageHandler());
		Jazmin.addServer(server);
		RelayServer relayServer=new RelayServer();
		relayServer.setHostAddress("10.44.218.63");
		Jazmin.addServer(relayServer);
		//
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
