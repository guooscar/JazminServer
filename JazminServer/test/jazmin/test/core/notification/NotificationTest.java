/**
 * 
 */
package jazmin.test.core.notification;

import jazmin.core.Jazmin;

/**
 * @author yama
 *
 */
public class NotificationTest {
	//
	public static void main(String[] args) throws Exception{
		Jazmin.notificationCenter.register(new SimpleNotification());
		Jazmin.start();
		//
		Thread.sleep(5000);
		Jazmin.notificationCenter.post("test");
	}
}
