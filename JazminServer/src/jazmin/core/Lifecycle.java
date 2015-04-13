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
	boolean inited;
	protected final  boolean isStarted(){
		return started;
	}
	protected final boolean isInited(){
		return inited;
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
	public void setLifecycleListener(LifecycleListener lifecycleListener) {
		this.lifecycleListener = lifecycleListener;
	}
	
}
