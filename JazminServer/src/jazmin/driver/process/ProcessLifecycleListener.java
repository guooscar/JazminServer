/**
 * 
 */
package jazmin.driver.process;

/**
 * @author yama
 *
 */
public interface ProcessLifecycleListener {
	void processStarted(ProcessInfo pi)throws Exception;
	void processDestroyed(ProcessInfo pi)throws Exception;
}
