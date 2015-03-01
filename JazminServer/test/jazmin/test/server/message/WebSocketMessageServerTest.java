/**
 * 
 */
package jazmin.test.server.message;

import jazmin.core.Jazmin;
import jazmin.server.msg.MessageServer;
import jazmin.server.msg.WebSocketMessageServer;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class WebSocketMessageServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageServer ms=new WebSocketMessageServer();
		ms.registerService(new TestService());
		Jazmin.addServer(ms);
		Jazmin.start();
		//{"si":"TestService.testMethod","ri":5,"rps":["1","2"]}
	}

}
