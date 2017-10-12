package jazmin.test.core.app;

import java.util.concurrent.TimeUnit;

import jazmin.core.app.AutoWired;
import jazmin.core.task.TaskDefine;
import jazmin.server.rpc.RpcService;

/**
 * 
 * @author yama
 * 31 Mar, 2015
 */

public interface TestAction {
	
	void testRpcMethod();
	//
	@RpcService
	public class TestActionImpl implements TestAction{
		@AutoWired
		TestService testService;
		//
		public void testRpcMethod(){
			
		}
		//
		@TaskDefine(initialDelay=0,period=10,runInThreadPool=true,unit=TimeUnit.SECONDS)
		public void testTask(){
			
		}
		//
	}
}
