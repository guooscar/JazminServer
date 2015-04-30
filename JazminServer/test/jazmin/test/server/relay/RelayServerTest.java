/**
 * 
 */
package jazmin.test.server.relay;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;

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
			server.setIdleTime(30000);
			Jazmin.addServer(new ConsoleServer());
			Jazmin.addServer(server);
			Jazmin.start();
			RelayChannel rc1=server.createRelayChannel(TransportType.UDP);
			RelayChannel rc2=server.createRelayChannel(TransportType.SCTP);
			rc1.link(rc2);
		}

}
