package jazmin.driver.mq.memory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;

/**
 * 
 * @author yama
 *
 */
public class MemoryTopicQueue extends TopicQueue{
	Map<String, MessagePayload>payloadMap;
	private LongAdder messageId;
	//
	//
	public MemoryTopicQueue(String id) {
		super(id,MessageQueueDriver.TOPIC_QUEUE_TYPE_MEMORY);
		maxTtl=1000*60;//1 min
		redelieverInterval=1000*5;//5 seconds redeliever 
		payloadMap=new ConcurrentHashMap<>();
		messageId=new LongAdder();
	}
	//
	@Override
	public void start() {
		super.start();
		topicSubscribers.forEach(s->{
			MemoryTopicChannel c=new MemoryTopicChannel(this,s);
			topicChannels.put(s.id,c);
		});
	}
	//
	public void publish(Object obj){
		super.publish(obj);
		//
		MessagePayload payload=new MessagePayload();
		payload.id=UUID.randomUUID().toString().replaceAll("-", "");
		payload.payload=obj;
		payload.subscriberCount=topicSubscribers.size();
		payloadMap.put(payload.id, payload);
		//
		topicSubscribers.forEach(s->{
			TopicMessage m=new TopicMessage();
			messageId.increment();
			m.id=messageId.longValue();
			m.payloadId=payload.id;
			m.subscriber=s.id;
			((MemoryTopicChannel)topicChannels.get(s)).append(m);
		});
	}
	
}
