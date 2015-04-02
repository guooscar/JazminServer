/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.core.Jazmin;
import jazmin.driver.rpc.JazminRPCDriver;


/**
 * @author yama
 * 23 Dec, 2014
 */
public class RPCClientTest {
	//
	public static void main(String[] args) throws Exception{
		JazminRPCDriver driver=new JazminRPCDriver();
		driver.addRemoteServer("1","1", "localhost", 6001);
		Jazmin.addDriver(driver);
		Jazmin.start();
		driver.create(TestRemoteService.class,"1").timeoutMethod();
	}
}
