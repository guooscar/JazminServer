/**
 * 
 */
package jazmin.test.driver.rpc;

import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.driver.rpc.JazminRPCDriver;
import jazmin.log.LoggerFactory;
import jazmin.test.server.rpc.TestRemoteServiceAsync;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class RPCDriverTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JazminRPCDriver driver=new JazminRPCDriver();
		driver.setPrincipal("a"+System.currentTimeMillis());
		driver.addRemoteServer("test","test","123","localhost",6001);
		Jazmin.addDriver(driver);
		
		Jazmin.start();
		LoggerFactory.setLevel("WARN");
		//
		//TestRemoteService ts=driver.create(TestRemoteService.class, "test");
		//for(int i=0;i<1000000;i++){
		//	ts.methodA();
		//}
		TestRemoteServiceAsync async=driver.createAsync(TestRemoteServiceAsync.class, "test");
		for(int i=0;i<10000000;i++){
			try{
				TimeUnit.NANOSECONDS.sleep(1);
				for(int j=0;j<15;j++){
					async.methodA((v,e)->{});
				}
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
		}
	}

}
