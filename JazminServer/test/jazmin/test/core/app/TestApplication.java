/**
 * 
 */
package jazmin.test.core.app;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.core.app.AutoWired;
import jazmin.server.rpc.RpcServer;
import jazmin.test.core.app.TestAction.TestActionImpl;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestApplication extends Application {
	@AutoWired
	static TestActionImpl testAction;
	//
	@Override
	public void init() throws Exception {
		createWired(TestApplication.class);
		register();
	}
	//
	public void start() {
		System.err.println(testAction.testService);
		System.err.println(testAction.testService.testDAO);
		System.err.println(testAction.testService.testDAO.connectionDriver);
	}
	//
	public static void main(String[] args)throws Exception{
		TestApplication ta=new TestApplication();
		Jazmin.loadApplication(ta);
		Jazmin.addServer(new RpcServer());
		Jazmin.start();
	}
}
