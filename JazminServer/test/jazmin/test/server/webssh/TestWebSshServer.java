package jazmin.test.server.webssh;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.webssh.HostInfoProvider;
import jazmin.server.webssh.WebSshServer;

/**
 * 
 * @author yama
 *
 */
public class TestWebSshServer {
	//
	public static void main(String[] args) {
		LoggerFactory.setLevel("DEBUG");
		WebSshServer server=new WebSshServer();
		server.setHostInfoProvider(new HostInfoProvider(){
			@Override
			public HostInfo getHostInfo(String token) {
				System.err.println(token);
				HostInfo localhost=new HostInfo();
				localhost.host="localhost";
				localhost.port=22;
				localhost.user="user";
				localhost.password="password";
				localhost.enableInput=true;
				return localhost;
			}
		});
		server.setPort(9999);
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
