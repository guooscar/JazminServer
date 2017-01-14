package jazmin.test.server.websockify;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
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
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
