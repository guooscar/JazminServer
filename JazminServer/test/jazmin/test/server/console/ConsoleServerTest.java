/**
 * 
 */
package jazmin.test.server.console;

import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class ConsoleServerTest  {
	public static void main(String[] args) throws Exception{
		ConsoleServer cs=new ConsoleServer();
		cs.init();
		cs.start();
	}
}
