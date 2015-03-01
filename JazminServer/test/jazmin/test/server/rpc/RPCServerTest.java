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
		Jazmin.start();
		RPCServer rpcServer=new RPCServer();
		rpcServer.init();
		rpcServer.start();
		//
		rpcServer.registerService(new TestRemoteServiceImpl());
	}
}
