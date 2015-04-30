/**
 * 
 */
package jazmin.test.server.relay;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.HexDumpRelayChannel;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;

/**
 * @author g2131
 *
 */
public class RelayServerTcpTest {

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
			RelayChannel rc1=server.createRelayChannel(TransportType.TCP);
			RelayChannel rc2=new HexDumpRelayChannel();
			rc1.relayTo(rc2);
		}

}
