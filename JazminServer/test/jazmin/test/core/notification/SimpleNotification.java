/**
 * 
 */
package jazmin.test.core.notification;

import jazmin.core.notification.Notification;
import jazmin.core.notification.NotificationDefine;

/**
 * @author yama
 *
 */
public class SimpleNotification {
	//
	@NotificationDefine(event="test",async=false)
	public void syncPrintArgs(Notification n){
		System.err.println(n.args.get("message"));
	}
	//
	@NotificationDefine(event="test",async=true)
	public void asyncPrintArgs(Notification n){
		System.err.println(n.args.get("message"));
	}
}
