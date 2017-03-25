package jazmin.test.server.webssh;

import java.io.FileOutputStream;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.webssh.ConnectionInfoProvider;
import jazmin.server.webssh.OutputStreamEndpoint;
import jazmin.server.webssh.JavaScriptChannelRobot;
import jazmin.server.webssh.WebSshChannel;
import jazmin.server.webssh.WebSshServer;
import jazmin.util.FileUtil;

/**
 * 
 * @author yama
 *
 */
public class TestWebSshServerFileEndpoint {
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
							FileUtil.getContent("misc/webssh/jsrobot.js"));
				} catch (Exception e) {
					e.printStackTrace();
				} 
				ConnectionInfo localhost=new ConnectionInfo();
				localhost.name="localhost";
				localhost.host="localhost";
				localhost.port=22;
				localhost.user="yama";
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
		//
		FileOutputStream fos=new FileOutputStream("/tmp/test.log",false);
		OutputStreamEndpoint fe=new OutputStreamEndpoint(fos);
		WebSshChannel channel=new WebSshChannel(server);
		channel.endpoint=fe;
		channel.setConnectionInfo(server.getConnectionInfoProvider().getConnectionInfo("localhost"));
		channel.startShell();
		server.addChannel(channel);
	}
}
