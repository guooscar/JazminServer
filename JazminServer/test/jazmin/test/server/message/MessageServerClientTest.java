/**
 * 
 */
package jazmin.test.server.message;

import jazmin.server.msg.client.MessageClient;
import jazmin.server.msg.codec.RequestMessage;

/**
 * @author yama
 *
 */
public class MessageServerClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageClient mc=new MessageClient();
		mc.connect("localhost",3001);
		RequestMessage rm=new RequestMessage();
		rm.serviceId="test";
		rm.requestId=1;
		rm.requestParameters=new String[]{"1","2","3"};
		mc.send(rm);
	}

}
