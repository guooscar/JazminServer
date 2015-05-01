/**
 * 
 */
package jazmin.server.relay;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author yama
 *
 */
public abstract  class NetworkRelayChannel extends RelayChannel{
	final String localHostAddress;
	final int localPort;
	InetSocketAddress remoteAddress;
	//
	Channel outboundChannel;
	final TransportType transportType;
	//
	public NetworkRelayChannel(TransportType type,String localAddress,  int localPort) {
		super();
		this.transportType=type;
		this.localHostAddress=localAddress;
		this.localPort=localPort;

	}

	/**
	 * @return the localHostAddress
	 */
	public String getLocalHostAddress() {
		return localHostAddress;
	}
	
	public boolean isActive(){
		if(outboundChannel==null){
			return false;
		}
		return outboundChannel.isActive();
	}
	//
	public void close()throws Exception{
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
	 * @return the transportType
	 */
	public TransportType getTransportType() {
		return transportType;
	}

	/* 	 */
	@Override
	public String getInfo() {
		String remoteAddressStr="";
		if(remoteAddress!=null){
			remoteAddressStr=remoteAddress.getAddress().getHostAddress()
					+":"+remoteAddress.getPort();
		}
		return transportType+"["+localHostAddress+":"+localPort+"<-->"+remoteAddressStr+"]";
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		String removeAddressStr="";
		if(remoteAddress!=null){
			removeAddressStr=remoteAddress.getAddress().getHostAddress()
					+":"+remoteAddress.getPort();
		}
		return name+"["+removeAddressStr+"<-->"+localHostAddress+":"+localPort+"]";
	}
}
