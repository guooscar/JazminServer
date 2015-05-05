/**
 * 
 */
package jazmin.test.server.sip.b2bua;

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
public class SipApp extends Application{
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
		String ipAddress="10.44.218.119";
		LoggerFactory.setLevel("DEBUG");
		SipServer server=new SipServer();
		server.setHostAddress(ipAddress);
		server.setPublicAddress(ipAddress);
		server.setMessageHandler(new B2BUAMessageHandler());
		Jazmin.addServer(server);
		RelayServer relayServer=new RelayServer();
		relayServer.addHostAddress(ipAddress);//interface 1
		relayServer.addHostAddress(ipAddress);//interface 2
		Jazmin.addServer(relayServer);
		//
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
