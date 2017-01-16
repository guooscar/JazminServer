package jazmin.test.server.webssh;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.webssh.ConnectionInfoProvider;
import jazmin.server.webssh.JavaScriptChannelRobot;
import jazmin.server.webssh.WebSshServer;
import jazmin.util.FileUtil;

/**
 * 
 * @author yama
 *
 */
public class TestWebSshServer {
	//
	public static void main(String[] args)throws Exception {
		//
		LoggerFactory.setLevel("DEBUG");
		WebSshServer server=new WebSshServer();
		
		server.setConnectionInfoProvider(new ConnectionInfoProvider(){
			@Override
			public ConnectionInfo getConnectionInfo(String token) {
				JavaScriptChannelRobot robot=null;
				try {
					robot = new JavaScriptChannelRobot(
							FileUtil.getContent("misc/webssh/demo.js"));
				} catch (Exception e) {
					e.printStackTrace();
				} 
				ConnectionInfo localhost=new ConnectionInfo();
				localhost.name="gskj";
				localhost.host="localhost";
				localhost.port=22;
				localhost.user="root";
				localhost.password="password";
				localhost.enableInput=true;
				localhost.channelListener=robot;
				return localhost;
			}
		});
		server.setPort(9999);
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
