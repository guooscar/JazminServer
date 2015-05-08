/**
 * 
 */
package jazmin.server.relay.misc;

import jazmin.server.relay.RelayChannel;
import jazmin.server.relay.RelayServer;


/**
 * @author yama
 *
 */
public class EchoRelayChannel extends RelayChannel{
	RelayChannel relayChannel;
	//
	public EchoRelayChannel(RelayServer server) {
		super(server);
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte buffer[]) throws Exception{
		super.dataFromRelay(channel, buffer);
		this.relayChannel=channel;
		channel.dataFromRelay(this, buffer);
	}
	@Override
	public String getInfo() {
		return "echo to:"+(relayChannel==null?"null":relayChannel.getId()+"/"+relayChannel.getName());
	}
}
