/**
 * 
 */
package jazmin.test.server.message;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.msg.MessageServer;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class WebSocketMessageServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageServer ms=new MessageServer();
		ms.setWebSocketPort(1443);
		ms.registerService(new TestService());
		Jazmin.addServer(ms);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
		//{"si":"TestService.testMethod","ri":5,"rps":["1","2"]}
	}

}
