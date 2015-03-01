/**
 * 
 */
package jazmin.core.app;

import jazmin.core.Lifecycle;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class Application extends Lifecycle {
	//
	@Override
	public String info() {
		return "Application class:"+getClass().getName();
	}
}
