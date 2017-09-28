/**
 * 
 */
package jazmin.driver.mq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.ning.http.client.providers.netty.chmv8.LongAdder;

/**
 * @author yama
 *
 */
public abstract class TopicQueue {
	private static Logger logger=LoggerFactory.get(TopicQueue.class);
	//
	protected String id;
	protected String type;
	protected LongAdder publishCount;
	protected Set<Short> topicSubscribers;
	protected Map<Long,Long>acceptSet;
	protected Map<Long,Long>rejectSet;
	protected Object lockObject=new Object();
	protected LongAdder delieverCount;
	protected LongAdder acceptCount;
	protected LongAdder rejectCount;
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
		acceptSet=new HashMap<>();
		rejectSet=new HashMap<>();
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
	public long getDelieverCount(){
		return delieverCount.longValue();
	}
	//
	public long getRejectCount(){
		return rejectCount.longValue();
	}
	//
	public long getAcceptCount(){
		return acceptCount.longValue();
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
	//
	public void reject(long id){
		synchronized (lockObject) {
			rejectSet.put(id, System.currentTimeMillis());
		}
	}
	//
	public void accept(long id){
		synchronized (lockObject) {
			acceptSet.put(id, System.currentTimeMillis());
		}
	}
	//
	void checkSet(){
		synchronized (lockObject) {
			checkMap(acceptSet);
			checkMap(rejectSet);
		}
	}
	//
	void checkMap(Map<Long,Long>map){
		long now=System.currentTimeMillis();
		List<Long>removedKeys=new LinkedList<>();
		for(Entry<Long,Long>e:rejectSet.entrySet()){
			if((now-e.getValue())>accpetRejectExpiredTime){
				long uuid=e.getKey();
				removedKeys.add(uuid);
				logger.warn("bad message id {} {}",id,uuid);
			}
		}
		//
		removedKeys.forEach(s->{map.remove(s);});
	}
}
