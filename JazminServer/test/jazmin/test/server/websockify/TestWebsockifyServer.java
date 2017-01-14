package jazmin.test.server.websockify;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.websockify.HostInfoProvider.HostInfo;
import jazmin.server.websockify.WebsockifyServer;

/**
 * 
 * @author yama
 *
 */
public class TestWebsockifyServer {
	//
	public static void main(String[] args) {
		LoggerFactory.setLevel("DEBUG");
		WebsockifyServer server=new WebsockifyServer();
		server.setHostInfoProvider(token->{
			HostInfo hi=new HostInfo();
			hi.host="localhost";
			hi.port=5900;
			return hi;
		});
		server.setPort(7777);
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
