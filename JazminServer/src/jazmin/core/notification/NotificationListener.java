/**
 * 
 */
package jazmin.core.notification;

import java.lang.reflect.Method;


/**
 * @author yama
 *
 */
public class NotificationListener {
	public String id;
	public String event;
	public Object instance;
	public Method method;
	public boolean async;
}
