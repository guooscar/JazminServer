/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rpc.RpcServer;
import jazmin.test.server.rpc.TestRemoteService.TestRemoteServiceImpl;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class RPCServerTest {
	//
	public static void main(String[] args) throws Exception{
		LoggerFactory.setLevel("OFF");
		RpcServer rpcServer=new RpcServer();
		rpcServer.setEnableSSL(false);
		//rpcServer.setCredential("123");
		Jazmin.dispatcher.setPerformanceLogFile("/tmp/test.log");
		Jazmin.addServer(rpcServer);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
		//
		rpcServer.registerService(new TestRemoteServiceImpl());
	}
}
