/**
 * 
 */
package jazmin.server.relay;


/**
 * @author yama
 *
 */
public class EchoRelayChannel extends RelayChannel{
	RelayChannel relayChannel;
	//
	public EchoRelayChannel() {
		super();
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
		return "echo to:"+(relayChannel==null?"null":relayChannel.id+"/"+relayChannel.name);
	}
}
