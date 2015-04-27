/**
 * 
 */
package jazmin.test.server.relay;

import jazmin.core.Jazmin;
import jazmin.server.relay.RelayServer;

/**
 * @author g2131
 *
 */
public class RelayServerTest {

	//-------------------------------------------------------------------------
		/**
		 * @param args
		 */
		public static void main(String[] args)throws Exception {
			RelayServer server=new RelayServer();
			server.setHostAddress("10.44.218.63");
			Jazmin.addServer(server);
			Jazmin.start();
			server.createRelayChannel();
		}

}
