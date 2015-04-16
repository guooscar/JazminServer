/**
 * 
 */
package jazmin.test.server.message;

import jazmin.core.Jazmin;
import jazmin.server.msg.MessageServer;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class MessageServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageServer ms=new MessageServer();
		ms.setMessageType(MessageServer.MESSAGE_TYPE_ZJSON);
		ms.registerService(new TestService());
		Jazmin.addServer(ms);
		Jazmin.start();
		//{"si":"TestService.testMethod","ri":5,"rps":["1","2"]}
	}

}
