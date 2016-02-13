/**
 * 
 */
package jazmin.test.log;

import jazmin.core.Jazmin;
import jazmin.driver.rpc.JazminRpcDriver;
import jazmin.log.LoggerFactory;
import jazmin.server.web.WebServer;

/**
 * @author yama
 * 25 Jan, 2016
 */
public class TestLoggerFactory {

	
	public static void main(String[] args) throws Exception{
		LoggerFactory.setFile("/tmp/test.log", true);
		//
		Jazmin.setServerName("wypd-"+System.getProperty("user.name"));
		Jazmin.environment.put("debug", "true");
		//
		JazminRpcDriver rpcDriver=new JazminRpcDriver();
		rpcDriver.addRemoteServer("jazmin://wx.51partner.com:9311/"+"WypdBizSystem"+"/app1");
		//rpcDriver.addRemoteServer("jazmin://192.168.3.106:6003/"+Actions.CLUSTER_BIZ+"/app1");
		Jazmin.addDriver(rpcDriver);
		//
		WebServer ws=new WebServer();
		ws.addResource("/","/Users/yama/Documents/JavaWorkspace/WypdWxSystem/release/WypdWxSystem");
		//
		ws.setPort(7002);
		Jazmin.addServer(ws);
		Jazmin.start();
	}

}
