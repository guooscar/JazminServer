/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.RelayServer;
import jazmin.server.sip.SipServer;

/**
 * @author yama
 * 26 Apr, 2015
 */
public class B2BUADemo extends Application{
	@Override
	public void start() throws Exception {
		SipServer server=Jazmin.getServer(SipServer.class);
		server.setMessageHandler(new B2BUAMessageHandler());
	}
	//
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
		//relayServer.setHostAddress("10.44.218.63");
		relayServer.addHostAddress("10.44.218.63");//interface 1
		relayServer.addHostAddress("10.44.218.63");//interface 2
		Jazmin.addServer(relayServer);
		//
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
