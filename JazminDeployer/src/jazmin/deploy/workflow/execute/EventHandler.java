/**
 * 
 */
package jazmin.deploy.workflow.execute;

/**
 * @author yama
 *
 */
public interface EventHandler {
	void onEvent(ExecuteContext ctx,String eventType);
}
