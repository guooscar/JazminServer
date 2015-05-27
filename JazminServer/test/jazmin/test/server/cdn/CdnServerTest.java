/**
 * 
 */
package jazmin.test.server.cdn;

import jazmin.core.Jazmin;
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
		final String DEFAULT_UA="Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; "
				+ "Trident/4.0; Acoo Browser 1.98.744; .NET CLR 3.5.30729)";
		Jazmin.addServer(new ConsoleServer());
		CdnServer cdnServer=new CdnServer();
		cdnServer.setPort(8080);
		cdnServer.setUserAgent(DEFAULT_UA);
		cdnServer.setHomeDir("d:/cdn-test");
		cdnServer.setOrginSiteURL("http://mirrors.163.com/");
		Jazmin.addServer(cdnServer);
		//
		//cdnServer.setRequestFilter(new BasicAuthFilter("abc","1234"));
		//
		Jazmin.start();
	}
}
