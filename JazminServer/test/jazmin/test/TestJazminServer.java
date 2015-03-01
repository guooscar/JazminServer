/**
 * 
 */
package jazmin.test;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.msg.MessageServer;
import jazmin.server.rpc.RPCServer;
import jazmin.test.server.rpc.TestRemoteServiceImpl;

/**
 * @author yama
 * 27 Dec, 2014
 */
public class TestJazminServer {
	public static void main(String[] args) throws Exception{
		//Jazmin.boot("rc/jazmin.js");
		//Jazmin.loadApplication("/Users/yama/Desktop/SanGuoDataSystem.jaz");
		//
		//WebServer ws=new WebServer();
		//ws.addResource("/","/Users/yama/Documents/JavaWorkspace/DemoWebApplication/release/DemoWebSystem" );
		//ws.addResource("/","/Users/yama/Documents/JavaWorkspace/DemoBossSystem/release/JazminDeploySystem");
		
		//ws.addWar("/","Users/yama/Desktop/SanGuoWebSystem.war");
		Jazmin.addServer(new ConsoleServer());
		Jazmin.addServer(new MessageServer());
		//
		RPCServer server=new RPCServer();
		server.registerService(new TestRemoteServiceImpl());
		//
		Jazmin.addServer(server);
		Jazmin.start();
		//
		LoggerFactory.setLevel("WARN");
		//
	}
}