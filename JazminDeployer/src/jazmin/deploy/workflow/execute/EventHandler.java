/**
 * 
 */
package jazmin.deploy.workflow.execute;

/**
 * @author yama
 *
 */
public interface EventHandler {
	public static final String EVENT_TYPE_ENTER="enter";
	public static final String EVENT_TYPE_LEAVE="leave";
	//
	void onEvent(ExecuteContext ctx,String eventType);
}
