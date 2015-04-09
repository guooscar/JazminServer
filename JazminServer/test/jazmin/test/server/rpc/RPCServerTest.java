/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.core.Jazmin;
import jazmin.server.rpc.RPCServer;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class RPCServerTest {
	//
	public static void main(String[] args) throws Exception{
		RPCServer rpcServer=new RPCServer();
		rpcServer.setCredential("123");
		Jazmin.addServer(rpcServer);
		Jazmin.start();
		//
		rpcServer.registerService(new TestRemoteServiceImpl());
	}
}
