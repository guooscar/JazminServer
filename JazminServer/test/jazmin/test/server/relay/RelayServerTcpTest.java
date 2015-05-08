/**
 * 
 */
package jazmin.test.server.relay;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.relay.misc.HexDumpRelayChannel;

/**
 * @author yama
 *
 */
public class RelayServerTcpTest {

	//-------------------------------------------------------------------------
		/**
		 * @param args
		 */
		public static void main(String[] args)throws Exception {
			RelayServer server=new RelayServer();
			server.setHostAddress("10.44.218.119");
			server.setIdleTime(30000);
			Jazmin.addServer(new ConsoleServer());
			Jazmin.addServer(server);
			Jazmin.start();
			RelayChannel rc1=server.createRelayChannel(TransportType.TCP_MULTICAST);
			RelayChannel rc2=new HexDumpRelayChannel(server);
			RelayChannel rc3=server.createRelayChannel(TransportType.UDP_UNICAST);
			rc1.relayTo(rc2);
			rc3.relayTo(rc1);
		}

}
