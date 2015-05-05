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
	protected InetSocketAddress remoteAddress;
	//
	protected Channel outboundChannel;
	protected final TransportType transportType;
	//
	protected long packetPeerCount;
	protected long bytePeerCount;
	//
	//
	public NetworkRelayChannel(TransportType type,String localAddress,  int localPort) {
		super();
		this.transportType=type;
		this.localHostAddress=localAddress;
		this.localPort=localPort;
	}
	//
	public void dataFromPeer(byte bytes[]) throws Exception{
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
	
	@Override
	public boolean isActive(){
		if(outboundChannel==null){
			return false;
		}
		return outboundChannel.isActive();
	}
	//
	@Override
	public void closeChannel()throws Exception{
		if(outboundChannel!=null){
			outboundChannel.close().sync();
		}
	}
	/**
	 * @return the localPort
	 */
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * @return the remoteAddress
	 */
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	
	/**
	 * @param remoteAddress the remoteAddress to set
	 */
	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * @return the transportType
	 */
	public TransportType getTransportType() {
		return transportType;
	}

	/* 	 */
	@Override
	public String getInfo() {
		double bytePeerCnt=bytePeerCount;
		String networkInfo=packetPeerCount+"/"+String.format("%.2fKB",bytePeerCnt/1024);
		String remoteAddressStr="";
		if(remoteAddress!=null){
			remoteAddressStr=remoteAddress.getAddress().getHostAddress()
					+":"+remoteAddress.getPort();
		}
		return transportType+"["+localHostAddress+":"+localPort+"<-->"+remoteAddressStr+"] "+networkInfo;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return getInfo();
	}
}
