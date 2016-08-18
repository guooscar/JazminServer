/**
 * 
 */
package jazmin.test.server.web;

import jazmin.core.Jazmin;
import jazmin.driver.file.FileServerDriver;
import jazmin.driver.rpc.JazminRpcDriver;
import jazmin.server.web.WebServer;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class TestWebServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//
		Jazmin.setServerName("boss"+System.getProperty("user.name"));
		JazminRpcDriver rpcDriver=new JazminRpcDriver();
		rpcDriver.addRemoteServer("YcytBizSystem","app","skydu.local", 6003);
		//rpcDriver.addRemoteServer("jazmin://wx.yicyt.com:8601/"+YcytBizSystem.class.getSimpleName()+"/app1");
		Jazmin.addDriver(rpcDriver);
		//
		FileServerDriver fileServerDriver=new FileServerDriver();
		fileServerDriver.setHomeDir("local_file_storage");
		fileServerDriver.addServer("YcytFileSystem","wx.yicyt.com",8604,1);
		Jazmin.addDriver(fileServerDriver);
		//
		WebServer ws=new WebServer();
		//ws.setEnableJettyLogger(true);
		ws.addResource("/","/Users/yama/Documents/JavaWorkspace/YcytBossSystem/release/YcytBossSystem");
		Jazmin.addServer(ws);
		ws.setPort(7003);
		Jazmin.start();
		
	}

}
