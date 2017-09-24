package jazmin.driver.mq.memory;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

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
	private long maxTtl;
	private long redelieverInterval;
	private LinkedList<TopicMessage>topicQueue;
	private Set<String> topicSubscribers;
	//
	private Set<String>acceptSet;
	private Set<String>rejectSet;
	//
	//
	public MemoryTopicQueue(String id) {
		this.id=id;
		this.type=MessageQueueDriver.TOPIC_QUEUE_TYPE_MEMORY;
		maxQueueSize=10240;
		maxTtl=1000*60;//1 min
		redelieverInterval=1000*5;//5 seconds redeliever 
		payloadMap=new ConcurrentHashMap<>();
		topicQueue=new LinkedList<>();
		topicSubscribers=new TreeSet<String>();
		//
		acceptSet=new TreeSet<String>();
		rejectSet=new TreeSet<String>();
	}

	//
	public int length(){
		synchronized (topicQueue) {
			return topicQueue.size();
		}
	}
	//
	public void subscribe(String name){
		if(topicSubscribers.contains(name)){
			throw new IllegalArgumentException(name+" already exists");
		}
		topicSubscribers.add(name);
	}
	//
	public void publish(Object obj){
		if(obj==null){
			throw new NullPointerException("publish message can not be null");
		}
		if(topicSubscribers.isEmpty()){
			throw new IllegalArgumentException("no topic subscriber");
		}
		if(topicQueue.size()>maxQueueSize){
			throw new IllegalStateException("topic queue "+id+" full "+maxQueueSize);
		}
		//
		MessagePayload payload=new MessagePayload();
		payload.id=UUID.randomUUID().toString();
		payload.payload=obj;
		payload.subscriberCount=topicSubscribers.size();
		payloadMap.put(payload.id, payload);
		//
		topicSubscribers.forEach(s->{
			TopicMessage m=new TopicMessage();
			m.id=UUID.randomUUID().toString();
			m.payloadId=payload.id;
			m.subscriber=s;
			m.sendTime=System.currentTimeMillis();
			m.ttl=maxTtl;
			synchronized (topicQueue) {
				topicQueue.add(m);
			}
		});
	}
	//
	public Message take(){
		synchronized (topicQueue) {
			if(topicQueue.isEmpty()){
				return null;
			}
			
			TopicMessage message=topicQueue.getFirst();
			MessagePayload payload=payloadMap.get(message.payloadId);
			if(acceptSet.contains(message.id)){
				payload.subscriberCount--;
				if(payload.subscriberCount<=0){
					payloadMap.remove(payload.id);
				}
				acceptSet.remove(message.id);
				topicQueue.removeFirst();
				return new Message();
			}
			//
			if(rejectSet.contains(message.id)){
				message.lastDeliverTime=System.currentTimeMillis();
				rejectSet.remove(message.id);
				return new Message();
			}
			//
			if((System.currentTimeMillis()-message.sendTime)>maxTtl){
				//max ttl 
				topicQueue.removeFirst();
				logger.warn("remove message for max ttl reached:"+message.id+" "+maxTtl);
				return new Message();
			}
			//
			if((System.currentTimeMillis()-message.lastDeliverTime)<redelieverInterval){
				return new Message();
			}
			message.deliverTimes++;
			message.lastDeliverTime=System.currentTimeMillis();
			//
			Message msg=new Message();
			msg.id=message.id;
			msg.delieverTimes=message.deliverTimes;
			msg.payload=payload.payload;
			msg.subscriber=message.subscriber;
			
			return msg;
		}
	}
	//
	public void reject(String id){
		synchronized (topicQueue) {
			rejectSet.add(id);
		}
	}
	//
	public void accept(String id){
		synchronized (topicQueue) {
			acceptSet.add(id);
		}
	}

	
}
