/**
 * 
 */
package jazmin.deploy.workflow.execute;

/**
 * @author yama
 *
 */
public interface ExceptionHandler {
	void onException(ExecuteContext ctx,Throwable e);
}
