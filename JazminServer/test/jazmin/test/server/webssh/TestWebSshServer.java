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
		JavaScriptChannelRobot robot=new JavaScriptChannelRobot(
				FileUtil.getContent("misc/webssh/jsrobot.js"));
		server.setConnectionInfoProvider(new ConnectionInfoProvider(){
			@Override
			public ConnectionInfo getConnectionInfo(String token) {
				ConnectionInfo localhost=new ConnectionInfo();
				localhost.host="localhost";
				localhost.port=22;
				localhost.user="yama";
				localhost.password="77585211";
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
