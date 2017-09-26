/**
 * 
 */
package jazmin.driver.mq;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.ning.http.client.providers.netty.chmv8.LongAdder;

/**
 * @author yama
 *
 */
public abstract class TopicQueue {
	protected String id;
	protected String type;
	protected LongAdder publishCount;
	protected Set<Short> topicSubscribers;
	public TopicQueue(String id,String type){
		this.id=id;
		this.type=type;
		topicSubscribers=new TreeSet<>();
		publishCount=new LongAdder();
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
	//
	public long getPublishCount(){
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
		publishCount.add(1);
		if(obj==null){
			throw new NullPointerException("publish message can not be null");
		}
		if(topicSubscribers.isEmpty()){
			throw new IllegalArgumentException("no topic subscriber");
		}
	}
	/**
	 * 
	 * @param subscriber
	 * @return
	 */
	public abstract Message take(short subscriber);
	public abstract void reject(String id);
	public abstract void accept(String id);
		
}
