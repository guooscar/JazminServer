/**
 * 
 */
package jazmin.test.server.web;

import jazmin.server.web.WebServer;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class TestWebServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		WebServer ws=new WebServer();
		ws.addResource("/","/Users/yama/Documents/JavaWorkspace/DemoBossSystem/release/DemoBossSystem");
		ws.init();
		ws.start();
		//
		
	}

}
