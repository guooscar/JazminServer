package jazmin.test.driver.rpc;

import jazmin.core.Jazmin;
import jazmin.driver.rpc.JazminRPCDriver;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rpc.RPCServer;
import jazmin.test.server.rpc.TestRemoteServiceAsync;
import jazmin.test.server.rpc.TestRemoteServiceImpl;

/**
 * 
 * @author yama
 * 16 Jan, 2015
 */
public class RPCDriverTest2 {

	public static void main(String[] args) {
		JazminRPCDriver driver=new JazminRPCDriver();
		driver.setPrincipal("a"+System.currentTimeMillis());
		driver.addRemoteServer("test","test","localhost",6001);
		Jazmin.addDriver(driver);
		Jazmin.addServer(new ConsoleServer());
		RPCServer rpcServer=new RPCServer();
		rpcServer.registerService(new TestRemoteServiceImpl());
		Jazmin.addServer(rpcServer);
		Jazmin.start();
		Logger logger=LoggerFactory.get(RPCDriverTest2.class);
		TestRemoteServiceAsync async=driver.createAsync(TestRemoteServiceAsync.class, "test");
		async.methodA((i,e)->{
			logger.info(i);
		});
		async.methodA((i,e)->{
			logger.info(i);
		});
	}

}
