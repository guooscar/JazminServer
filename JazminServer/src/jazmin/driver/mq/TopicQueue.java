/**
 * 
 */
package jazmin.driver.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;


/**
 * @author yama
 *
 */
public abstract class TopicQueue {
	//
	protected String id;
	protected String type;
	protected LongAdder publishCount;
	protected Set<Short> topicSubscribers;
	protected long maxTtl;
	protected long redelieverInterval;
	protected long accpetRejectExpiredTime=1000*60*10;
	protected Map<Short,TopicChannel>topicChannels;
	//
	public TopicQueue(String id,String type){
		this.id=id;
		this.type=type;
		topicSubscribers=new TreeSet<>();
		publishCount=new LongAdder();
		topicChannels=new HashMap<>();
		
	}
	public long getRedelieverInterval() {
		return redelieverInterval;
	}

	public void setRedelieverInterval(long redelieverInterval) {
		if(redelieverInterval<=0){
			throw new IllegalArgumentException("redelieverInterval should >0");
		}
		this.redelieverInterval = redelieverInterval;
	}

	public long getMaxTtl() {
		return maxTtl;
	}

	public void setMaxTtl(long maxTtl) {
		if(maxTtl<=0){
			throw new IllegalArgumentException("maxTtl should >0");
		}
		this.maxTtl = maxTtl;
	}
	//
	public String getId(){
		return id;
	}
	//
	public String getType() {
		return type;
	}
	//
	public void start(){
		
	}
	public void stop(){
		
	}
	//
	public long getPublishedCount(){
		return publishCount.longValue();
	}
	/**
	 * 
	 * @param name
	 */
	public void subscribe(short name){
		if(topicSubscribers.contains(name)){
			throw new IllegalArgumentException(name+" already exists");
		}
		topicSubscribers.add(name);
	}
	/**
	 * 
	 * @param obj
	 */
	public void publish(Object obj){
		if(obj==null){
			throw new NullPointerException("publish message can not be null");
		}
		if(topicSubscribers.isEmpty()){
			throw new IllegalArgumentException("no topic subscriber");
		}
		publishCount.add(1);
	}
	//
	public Message take(short subscriberId){
		TopicChannel channel =topicChannels.get(subscriberId);
		if(channel==null){
			throw new IllegalArgumentException(
					"can not find subscriber ["+subscriberId+"] on topic queue:"+id);
		}
		Message message= channel.take();
		if(message!=null){
			message.topic=id;
		}
		return message;
	}
	//
	public void accept(short subscriberId,long messageId){
		TopicChannel channel =topicChannels.get(subscriberId);
		if(channel==null){
			throw new IllegalArgumentException("can not find subscriber on topic queue:"+id);
		}
		channel.accept(messageId);
	}
	//
	public void reject(short subscriberId,long messageId){
		TopicChannel channel =topicChannels.get(subscriberId);
		if(channel==null){
			throw new IllegalArgumentException("can not find subscriber on topic queue:"+id);
		}
		channel.reject(messageId);
	}
}
