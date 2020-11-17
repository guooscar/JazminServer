/**
 * 
 */
package jazmin.test.core.app;

import jazmin.core.app.Application;
import jazmin.core.app.AutoWired;
import jazmin.test.core.app.TestAction.TestActionImpl;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestApplication extends Application {
	@AutoWired
	TestActionImpl testAction;
	//
	@Override
	public void init() throws Exception {
		
	}
	//
	public void start() {
		System.err.println(testAction.testService);
		System.err.println(testAction.testService.testDAO);
		System.err.println(testAction.testService.testDAO.connectionDriver);
	}
	//
	public static void runAccount() throws Exception{
		
	}
	//
	public static void runBiz() throws Exception{
		
	}
	//
	public static void main(String[] args)throws Exception{
		runAccount();
	}
}
