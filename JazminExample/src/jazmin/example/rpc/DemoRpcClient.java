/**
 * 
 */
package jazmin.example.rpc;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.driver.rpc.JazminRpcDriver;
import jazmin.example.rpc.DemoRpcService.DemoRpcServiceAsync;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class DemoRpcClient extends Application{
	//
	public static void main(String[] args) throws Exception{
		LoggerFactory.setLevel("ALL");
		JazminRpcDriver rpcDriver=new JazminRpcDriver();
		rpcDriver.addRemoteServer("jazmin://127.0.0.1:6001/test/app1");
		Jazmin.addDriver(rpcDriver);
		Jazmin.start();
		//
		DemoRpcService service=rpcDriver.create(DemoRpcService.class,"test");
		String echo=service.echo("test");
		System.out.println(echo);
		//
		DemoRpcServiceAsync serviceAsync=rpcDriver.createAsync(DemoRpcServiceAsync.class,"test");
		serviceAsync.echo("123",(rsp,e)->{
			System.out.println(rsp);
		});
		
	}
}
