/**
 * 
 */
package jazmin.test.server.web;

import jazmin.core.Jazmin;
import jazmin.driver.rpc.JazminRpcDriver;
import jazmin.log.LoggerFactory;
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
		LoggerFactory.setLevel("ALL");
		LoggerFactory.setFile("/tmp/" + TestWebServer.class.getSimpleName() + ".log", true);
		//
		//
		Jazmin.setServerName("boss"+System.getProperty("user.name"));
		JazminRpcDriver rpcDriver=new JazminRpcDriver();
        rpcDriver.addRemoteServer("TableItAccountSystem", "app", "127.0.0.1", 9996);
        rpcDriver.addRemoteServer("TableItBizSystem_uat_0", "app", "127.0.0.1", 9997);
        Jazmin.addDriver(rpcDriver);
		//
//		FileServerDriver fileServerDriver=new FileServerDriver();
//		fileServerDriver.setHomeDir("local_file_storage");
//		fileServerDriver.addServer("YcytFileSystem","wx.yicyt.com",8604,1);
//		Jazmin.addDriver(fileServerDriver);
		//
		WebServer ws=new WebServer();
		ws.setEnableJettyLogger(true);
		ws.addResource("/","/Users/skydu/eclipse-workspace11/TableItWebSystem/release");
		Jazmin.addServer(ws);
		ws.setPort(7003);
		Jazmin.start();
		
	}

}
