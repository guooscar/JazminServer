/**
 * 
 */
package jazmin.core.notification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 *
 */
public class Notification {
	public String event;
	public Map<String,Object>args;
	public Notification() {
		args=new HashMap<String, Object>();
	}
}
