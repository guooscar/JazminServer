/**
 * 
 */
package jazmin.server.relay;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yama
 * 26 Apr, 2015
 */
public abstract class RelayChannel {
	//
	String id;
	//
	protected final long createTime;
	long lastAccessTime;
	//
	protected long packetRelayCount;
	protected long byteRelayCount;
	//
	protected String name;
	
	protected List<RelayChannel>linkedChannels;
	static AtomicInteger channelId=new AtomicInteger();
	//
	public RelayChannel(RelayServer server) {
		createTime=System.currentTimeMillis();
		lastAccessTime=createTime;
		linkedChannels=new LinkedList<RelayChannel>();
		id=channelId.incrementAndGet()+"";
		name=id+"";
		server.addChannel(this);
	}
	//--------------------------------------------------------------------------
	//
	public void bidiRelay(RelayChannel channel){
		relayTo(channel);
		channel.relayTo(this);
	}
	//
	public void relayFrom(RelayChannel channel){
		channel.relayTo(this);
	}
	//
	public void relayTo(RelayChannel channel){
		synchronized (linkedChannels) {
			if(linkedChannels.contains(channel)){
				throw new IllegalStateException("already linked");
			}
			linkedChannels.add(channel);
		}
	}
	//
	public void unRelay(RelayChannel channel){
		synchronized (linkedChannels) {
			linkedChannels.remove(channel);
		}
	}
	//
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
	//
	public void dataFromRelay(RelayChannel channel,byte buffer[])
			throws Exception{
		lastAccessTime=System.currentTimeMillis();
		packetRelayCount++;
		byteRelayCount+=buffer.length;
	}
	//--------------------------------------------------------------------------
	//
	public boolean isActive(){
		return true;
	}
	//
	public void closeChannel()throws Exception{
		
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
	 * @return the packetRelayCount
	 */
	public long getPacketRelayCount() {
		return packetRelayCount;
	}
	/**
	 * @return the byteRelayCount
	 */
	public long getByteRelayCount() {
		return byteRelayCount;
	}
	/**
	 * @return the linkedChannels
	 */
	public List<RelayChannel> getLinkedChannels() {
		return new LinkedList<RelayChannel>(linkedChannels);
	}
	//
	public abstract String getInfo();
}
