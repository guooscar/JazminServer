/**
 * 
 */
package jazmin.test.core.app;

import jazmin.core.app.Application;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestApplication extends Application {
	@Override
	public void init() throws Exception {
		TestActionImpl action=createWired(TestActionImpl.class);
		//
		System.out.println(action.testService);
		System.out.println(action.testService.testDAO);
		System.out.println(action.testService.testDAO.connectionDriver);
		
	}
	//
	public static void main(String[] args)throws Exception{
		TestApplication ta=new TestApplication();
		ta.init();
	}
}
