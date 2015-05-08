/**
 * 
 */
package jazmin.server.relay;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public abstract class NetworkRelayChannel extends RelayChannel{
	private static Logger logger=LoggerFactory.get(NetworkRelayChannel.class);
	//
	protected final String localHostAddress;
	protected final int localPort;
	//
	protected final TransportType transportType;
	//
	protected long packetPeerCount;
	protected long bytePeerCount;
	protected Channel serverChannel;
	//
	//
	public NetworkRelayChannel(
			RelayServer server,
			TransportType type,
			String localAddress, 
			int localPort) {
		super(server);
		this.transportType=type;
		this.localHostAddress=localAddress;
		this.localPort=localPort;
	}
	//
	public void dataFromPeer(InetSocketAddress remoteAddress,byte bytes[]) throws Exception{
		bytePeerCount+=bytes.length;
		packetPeerCount++;
		synchronized (linkedChannels) {
			for(RelayChannel rc:linkedChannels){
				rc.lastAccessTime=System.currentTimeMillis();
				try {
					rc.dataFromRelay(this,bytes);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}
	}
	/**
	 * @return the localHostAddress
	 */
	public String getLocalHostAddress() {
		return localHostAddress;
	}
	
	/**
	 * @return the localPort
	 */
	public int getLocalPort() {
		return localPort;
	}
	/**
	 * @return the transportType
	 */
	public TransportType getTransportType() {
		return transportType;
	}
	//
	@Override
	public void closeChannel() throws Exception {
		super.closeChannel();
		if(serverChannel!=null){
			serverChannel.close();
		}
	}
	/**
	 * @param serverChannel the serverChannel to set
	 */
	public void setServerChannel(Channel serverChannel) {
		this.serverChannel = serverChannel;
	}
	//
	@Override
	public boolean isActive() {
		if(serverChannel==null){
			return false;
		}
		return serverChannel.isActive();
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return getInfo();
	}
}
