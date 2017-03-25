/**
 * 
 */
package jazmin.test.server.message;

import jazmin.server.msg.client.MessageClient;

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
		mc.connect("uat.itit.io",8602);
		mc.invokeSync("ZjhService.loginByPassword", 
				new String[]{"90","57c23484a8b8991e8eb05371cb39792d","test"});
	}

}
