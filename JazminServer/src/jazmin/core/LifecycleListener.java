/**
 * 
 */
package jazmin.core;


/**
 * @author yama
 * 22 Dec, 2014
 */
public interface LifecycleListener {
	void beforeInit(Lifecycle server)throws Exception;
	void afterInit(Lifecycle server)throws Exception;
	void beforeStart(Lifecycle server)throws Exception;
	void afterStart(Lifecycle server)throws Exception;
	void beforeStop(Lifecycle server)throws Exception;
	void afterStop(Lifecycle server)throws Exception;
}
