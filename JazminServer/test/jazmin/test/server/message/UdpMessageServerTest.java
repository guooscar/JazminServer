/**
 * 
 */
package jazmin.test.server.message;

import jazmin.core.Jazmin;
import jazmin.server.msg.MessageServer;

/**
 * @author yama
 *
 */
public class UdpMessageServerTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageServer ms=new MessageServer();
		ms.setUdpPort(5555);
		ms.registerService(new TestService());
		Jazmin.addServer(ms);
		Jazmin.start();
	}
}
