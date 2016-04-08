package jazmin.test.server.file;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.file.FileServer;

/**
 * @author yama
 *
 */
public class FileServerTest {


	// --------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Jazmin.addServer(new ConsoleServer());
		FileServer cdnServer=new FileServer();
		cdnServer.setPort(8080);
		cdnServer.setHomeDir("/Users/yama/Desktop/file-server-test");
		Jazmin.addServer(cdnServer);
		Jazmin.start();
	}
}
