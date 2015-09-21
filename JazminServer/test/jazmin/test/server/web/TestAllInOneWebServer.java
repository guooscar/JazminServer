/**
 * 
 */
package jazmin.test.server.web;

import jazmin.core.Jazmin;
import jazmin.server.web.WebServer;

/**
 * @author yama
 *
 */
public class TestAllInOneWebServer {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WebServer ws=new WebServer();
		ws.setHttpsPort(8443);
		ws.setKeyStoreType("jks");
		ws.setKeyStoreFile("itit.io.jks");
		ws.setKeyStorePassword("urpassword");
		ws.addApplication("/","/");
		Jazmin.addServer(ws);
		Jazmin.loadApplication(new TestWebApplication());
		Jazmin.start();
	}

}
