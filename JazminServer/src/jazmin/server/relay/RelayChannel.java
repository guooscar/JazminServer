/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 26 Apr, 2015
 */
public abstract class RelayChannel {
	private static Logger logger=LoggerFactory.get(RelayChannel.class);
	//
	String id;
	//
	protected final long createTime;
	long lastAccessTime;
	//
	protected long packetReceiveCount;
	protected long byteReceiveCount;
	protected long packetSentCount;
	protected long byteSentCount;
	//
	protected String name;
	
	protected List<RelayChannel>linkedChannels;
	static AtomicInteger channelId=new AtomicInteger();
	//
	public RelayChannel() {
		createTime=System.currentTimeMillis();
		lastAccessTime=createTime;
		linkedChannels=new LinkedList<RelayChannel>();
		id=channelId.incrementAndGet()+"";
		name=id+"";
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
	public abstract void write(ByteBuf buffer)throws Exception;
	//--------------------------------------------------------------------------
	public  void read(ByteBuf buffer) throws Exception{
		lastAccessTime=System.currentTimeMillis();
		byteReceiveCount+=buffer.capacity();
		packetReceiveCount++;
		synchronized (linkedChannels) {
			for(RelayChannel rc:linkedChannels){
				rc.lastAccessTime=System.currentTimeMillis();
				try {
					rc.write(buffer);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}
	}
	//
	public boolean isActive(){
		return true;
	}
	//
	public void close()throws Exception{
		
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
	 * @return the linkedChannels
	 */
	public List<RelayChannel> getLinkedChannels() {
		return new LinkedList<RelayChannel>(linkedChannels);
	}
	//
	public abstract String getInfo();
}
