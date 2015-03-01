/**
 * 
 */
package jazmin.core;

/**
 * 
 * @author yama
 * 22 Dec, 2014
 */
public abstract class Lifecycle {
	//
	LifecycleListener lifecycleListener;
	boolean started;
	public boolean started(){
		return started;
	}
	//
	public void init() throws Exception{}
	public void start() throws Exception{}
	public void stop() throws Exception{}
	public String info(){
		return null;
	}
	//
	/**
	 * @param lifecycleListener the lifecycleListener to set
	 * @return 
	 */
	public void lifecycleListener(LifecycleListener lifecycleListener) {
		this.lifecycleListener = lifecycleListener;
	}
	
}
