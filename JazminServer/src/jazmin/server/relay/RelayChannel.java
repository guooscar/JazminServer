/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yama
 * 26 Apr, 2015
 */
public abstract class RelayChannel {
	int id;
	final String localHostAddress;
	final int localPort;
	InetSocketAddress remoteAddress;
	//
	long createTime;
	long lastAccessTime;
	//
	Channel outboundChannel;
	//
	long packetReceiveCount;
	long byteReceiveCount;
	long packetSentCount;
	long byteSentCount;
	//
	String name;
	final TransportType transportType;
	//
	List<RelayChannel>linkedChannels;
	static AtomicInteger channelId=new AtomicInteger();
	//
	RelayChannel(TransportType type,String localAddress,  int localPort) {
		this.transportType=type;
		this.localHostAddress=localAddress;
		this.localPort=localPort;
		createTime=System.currentTimeMillis();
		lastAccessTime=createTime;
		linkedChannels=new LinkedList<RelayChannel>();
		id=channelId.incrementAndGet();
		name=type+"-"+id;
	}
	//--------------------------------------------------------------------------
	//
	public void bidiLink(RelayChannel channel){
		link(channel);
		channel.link(this);
	}
	//
	public void link(RelayChannel channel){
		synchronized (linkedChannels) {
			if(linkedChannels.contains(channel)){
				throw new IllegalStateException("already linked");
			}
			linkedChannels.add(channel);
		}
	}
	//
	public void unLink(RelayChannel channel){
		synchronized (linkedChannels) {
			linkedChannels.remove(channel);
		}
	}
	/**
	 * @return the createTime
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * @return the lastAccessTime
	 */
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	
	/**
	 * @return the localHostAddress
	 */
	public String getLocalHostAddress() {
		return localHostAddress;
	}
	//
	void sendData(ByteBuf buffer){
		lastAccessTime=System.currentTimeMillis();
	}
	//--------------------------------------------------------------------------
	void receiveData(ByteBuf buffer){
		lastAccessTime=System.currentTimeMillis();
		byteReceiveCount+=buffer.capacity();
		packetReceiveCount++;
		synchronized (linkedChannels) {
			for(RelayChannel rc:linkedChannels){
				rc.sendData(buffer);
			}
		}
	}
	public boolean isActive(){
		if(outboundChannel==null){
			return false;
		}
		return outboundChannel.isActive();
	}
	//
	void close()throws Exception{
		if(outboundChannel!=null){
			outboundChannel.close().sync();
		}
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the packetReceiveCount
	 */
	public long getPacketReceiveCount() {
		return packetReceiveCount;
	}
	/**
	 * @return the byteReceiveCount
	 */
	public long getByteReceiveCount() {
		return byteReceiveCount;
	}
	/**
	 * @return the packetSentCount
	 */
	public long getPacketSentCount() {
		return packetSentCount;
	}
	/**
	 * @return the byteSentCount
	 */
	public long getByteSentCount() {
		return byteSentCount;
	}
	/**
	 * @return the transportType
	 */
	public TransportType getTransportType() {
		return transportType;
	}

	/**
	 * @return the linkedChannels
	 */
	public List<RelayChannel> getLinkedChannels() {
		return new LinkedList<RelayChannel>(linkedChannels);
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
