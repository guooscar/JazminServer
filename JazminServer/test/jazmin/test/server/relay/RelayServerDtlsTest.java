/**
 * 
 */
package jazmin.test.server.relay;

import jazmin.core.Jazmin;
import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.relay.misc.HexDumpRelayChannel;
import jazmin.server.relay.udp.DtlsRelayChannel;

/**
 * @author g2131
 *
 */
public class RelayServerDtlsTest {

	//-------------------------------------------------------------------------
		/**
		 * @param args
		 */
		public static void main(String[] args)throws Exception {
			RelayServer server=new RelayServer();
			server.setHostAddress("10.44.218.119");
			server.setIdleTime(30000);
			Jazmin.addServer(server);
			Jazmin.start();
			//
			DtlsRelayChannel webrtcAudio=(DtlsRelayChannel)server.createRelayChannel(TransportType.DTLS,"webrtcaAudio");
			DtlsRelayChannel webrtcVideo=(DtlsRelayChannel)server.createRelayChannel(TransportType.DTLS,"webrtcaVideo");
			//
			RelayChannel rtpAudioDump=new HexDumpRelayChannel(server,"audio.dump",true);
			RelayChannel rtpVideoDump=new HexDumpRelayChannel(server,"video.dump",true);
			//
			webrtcAudio.relayTo(rtpAudioDump);
			webrtcVideo.relayTo(rtpVideoDump);
			//
			System.out.println("webrtcAudio port:"+webrtcAudio.getLocalPort());
			System.out.println("webrtcVideo port:"+webrtcVideo.getLocalPort());
			//send data to these port and the dump channel will dump rtp packet as hex string
			
		}

}
