/**
 * 
 */
package jazmin.driver.mq;

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
	
	protected Object lockObject=new Object();
	protected LongAdder delieverCount;
	protected LongAdder acceptCount;
	protected LongAdder rejectCount;
	protected LongAdder expriedCount;
	protected long maxTtl;
	protected long redelieverInterval;
	
	//
	protected long accpetRejectExpiredTime=1000*60*10;
	//
	public TopicQueue(String id,String type){
		this.id=id;
		this.type=type;
		topicSubscribers=new TreeSet<>();
		publishCount=new LongAdder();
		delieverCount=new LongAdder();
		acceptCount=new LongAdder();
		rejectCount=new LongAdder();
		expriedCount=new LongAdder();
		
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
	public long getDelieveredCount(){
		return delieverCount.longValue();
	}
	//
	public long getRejectedCount(){
		return rejectCount.longValue();
	}
	//
	public long getAcceptedCount(){
		return acceptCount.longValue();
	}
	//
	public long getExpiredCount(){
		return expriedCount.longValue();
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
	//
	public abstract int length();
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
	/**
	 * 
	 * @param subscriber
	 * @return
	 */
	public abstract Message take();
	//
	public abstract void reject(long id);
	public abstract void accept(long id);
	protected void checkSet(){}
	//
}
