/**
 * 
 */
package jazmin.driver.mq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	protected List<TopicSubscriber> topicSubscribers;
	protected long maxTtl;
	protected long redelieverInterval;
	protected long accpetRejectExpiredTime=1000*60*10;
	protected Map<Short,TopicChannel>topicChannels;
	//
	public TopicQueue(String id,String type){
		this.id=id;
		this.type=type;
		topicSubscribers=new LinkedList<TopicSubscriber>();
		publishCount=new LongAdder();
		topicChannels=new HashMap<>();
		maxTtl=1000*3600*24;//1 day
		redelieverInterval=1000*5;//5 seconds redeliever 
	}
	/**
	 * 
	 */
	public List<TopicChannel>getChannels(){
		return new LinkedList<TopicChannel>(topicChannels.values());
	}
	/**
	 * 
	 * @return
	 */
	public long getRedelieverInterval() {
		return redelieverInterval;
	}
	/**
	 * 
	 * @param redelieverInterval
	 */
	public void setRedelieverInterval(long redelieverInterval) {
		if(redelieverInterval<=0){
			throw new IllegalArgumentException("redelieverInterval should >0");
		}
		this.redelieverInterval = redelieverInterval;
	}
	/**
	 * 
	 * @return
	 */
	public long getMaxTtl() {
		return maxTtl;
	}
	/**
	 * 
	 * @param maxTtl
	 */
	public void setMaxTtl(long maxTtl) {
		if(maxTtl<=0){
			throw new IllegalArgumentException("maxTtl should >0");
		}
		this.maxTtl = maxTtl;
	}
	/**
	 * 
	 * @return
	 */
	public String getId(){
		return id;
	}
	/**
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}
	/**
	 * 
	 */
	public void start(){
		
	}
	/**
	 * 
	 */
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
	public void subscribe(TopicSubscriber ts){
		if(topicSubscribers.contains(ts)){
			throw new IllegalArgumentException(ts.name+" already exists");
		}
		topicSubscribers.add(ts);
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
		publishCount.increment();
	}
	//
	public Message take(short subscriberId){
		TopicChannel channel =topicChannels.get(subscriberId);
		if(channel==null){
			throw new IllegalArgumentException(
					"can not find subscriber ["+subscriberId+"] on topic queue:"+id);
		}
		Message message= channel.take();
		if(message!=null&&message!=MessageQueueDriver.takeNext){
			message.topic=id;
			channel.delieverCount.increment();
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
