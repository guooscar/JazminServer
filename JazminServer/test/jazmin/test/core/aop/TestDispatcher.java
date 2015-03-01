/**
 * 
 */
package jazmin.test.core.aop;

import jazmin.core.aop.Dispatcher;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class TestDispatcher {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		Dispatcher d=new Dispatcher();
		TestService ts=new TestService();
		//
		d.invokeInPool("",ts,TestService.class.getMethod("methodB"));
	}

}
