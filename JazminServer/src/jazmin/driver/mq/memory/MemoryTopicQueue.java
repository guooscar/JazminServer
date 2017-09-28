package jazmin.driver.mq.memory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.ning.http.client.providers.netty.chmv8.LongAdder;

/**
 * 
 * @author yama
 *
 */
public class MemoryTopicQueue extends TopicQueue{
	private static Logger logger=LoggerFactory.get(MemoryTopicQueue.class);
	//
	private int maxQueueSize;
	private Map<String, MessagePayload>payloadMap;
	private LinkedList<TopicMessage>topicQueue;
	private LongAdder messageId;
	//
	protected Map<Long,Long>acceptSet;
	protected Map<Long,Long>rejectSet;
	//
	//
	public MemoryTopicQueue(String id) {
		super(id,MessageQueueDriver.TOPIC_QUEUE_TYPE_MEMORY);
		maxQueueSize=10240;
		maxTtl=1000*60;//1 min
		redelieverInterval=1000*5;//5 seconds redeliever 
		payloadMap=new ConcurrentHashMap<>();
		topicQueue=new LinkedList<>();
		messageId=new LongAdder();
		//
		acceptSet=new HashMap<>();
		rejectSet=new HashMap<>();
		
	}

	//
	public int length(){
		synchronized (lockObject) {
			return topicQueue.size();
		}
	}

	//
	public void publish(Object obj){
		super.publish(obj);
		if(topicQueue.size()>maxQueueSize){
			throw new IllegalStateException("topic queue "+id+" full "+maxQueueSize);
		}
		//
		MessagePayload payload=new MessagePayload();
		payload.id=UUID.randomUUID().toString().replaceAll("-", "");
		payload.payload=obj;
		payload.subscriberCount=topicSubscribers.size();
		payloadMap.put(payload.id, payload);
		//
		topicSubscribers.forEach(s->{
			TopicMessage m=new TopicMessage();
			m.id=UUID.randomUUID().toString();
			m.payloadId=payload.id;
			m.subscriber=s;
			synchronized (lockObject) {
				topicQueue.add(m);
			}
		});
	}
	//
	public Message take(){
		synchronized (lockObject) {
			if(topicQueue.isEmpty()){
				return null;
			}
			
			TopicMessage message=topicQueue.getFirst();
			MessagePayload payload=payloadMap.get(message.payloadId);
			if(acceptSet.containsKey(message.id)){
				payload.subscriberCount--;
				if(payload.subscriberCount<=0){
					payloadMap.remove(payload.id);
				}
				acceptSet.remove(message.id);
				topicQueue.removeFirst();
				return MessageQueueDriver.takeNext;
			}
			//
			if(rejectSet.containsKey(message.id)){
				message.lastDeliverTime=System.currentTimeMillis();
				rejectSet.remove(message.id);
				topicQueue.add(topicQueue.removeFirst());//move to last
				return  MessageQueueDriver.takeNext;
			}
			//
			if(message.lastDeliverTime>0){
				if((System.currentTimeMillis()-message.lastDeliverTime)>maxTtl){
					//max ttl 
					topicQueue.removeFirst();
					payload.subscriberCount--;
					if(payload.subscriberCount<=0){
						payloadMap.remove(payload.id);
					}
					logger.warn("remove message for max ttl reached:"+message.id+" "+maxTtl);
					return MessageQueueDriver.takeNext;
				}	
			}
			//
			if((System.currentTimeMillis()-message.lastDeliverTime)<redelieverInterval){
				topicQueue.add(topicQueue.removeFirst());
				return MessageQueueDriver.takeNext;
			}
			message.deliverTimes++;
			message.lastDeliverTime=System.currentTimeMillis();
			//
			Message msg=new Message();
			messageId.increment();
			msg.id=messageId.longValue();
			msg.delieverTimes=message.deliverTimes;
			msg.payload=payload.payload;
			msg.subscriber=message.subscriber;
			
			return msg;
		}
	}
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
	protected void checkSet(){
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
