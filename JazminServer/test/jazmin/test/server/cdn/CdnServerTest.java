/**
 * 
 */
package jazmin.test.server.cdn;

import jazmin.core.Jazmin;
import jazmin.server.cdn.CdnServer;
import jazmin.server.console.ConsoleServer;

/**
 * @author g2131
 *
 */
public class CdnServerTest {


	// --------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Jazmin.addServer(new ConsoleServer());
		CdnServer cdnServer=new CdnServer();
		cdnServer.setHomeDir("f:/");
		Jazmin.addServer(cdnServer);
		//
		Jazmin.start();
	}
}
