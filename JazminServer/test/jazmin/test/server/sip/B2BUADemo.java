/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
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
}
