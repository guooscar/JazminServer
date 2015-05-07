/**
 * 
 */
package jazmin.test.server.cdn;

import jazmin.core.Jazmin;
import jazmin.server.cdn.BasicAuthFilter;
import jazmin.server.cdn.CdnServer;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
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
		cdnServer.setHomeDir("d:/cdn-test");
		cdnServer.setOrginSiteURL("http://www.apache.org");
		Jazmin.addServer(cdnServer);
		//
		cdnServer.setRequestFilter(new BasicAuthFilter("abc","1234"));
		//
		Jazmin.start();
	}
}
