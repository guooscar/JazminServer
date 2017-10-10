/**
 * 
 */
package jazmin.driver.mq.memory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicChannel;
import jazmin.driver.mq.TopicQueue;
import jazmin.driver.mq.TopicSubscriber;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class MemoryTopicChannel extends TopicChannel{
	private static Logger logger=LoggerFactory.get(MemoryTopicChannel.class);
	//
	private LinkedList<TopicMessage>topicQueue;
	protected Map<Long,Long>acceptSet;
	protected Map<Long,Long>rejectSet;
	//
	public MemoryTopicChannel(TopicQueue queue,TopicSubscriber subscriber) {
		super(queue,subscriber);
		topicQueue=new LinkedList<>();
		acceptSet=new HashMap<>();
		rejectSet=new HashMap<>();

	}
	//
	public void append(TopicMessage message) {
		synchronized (lockObject) {
			topicQueue.add(message);		
		}
	}
	//
	//
	public Message take(){
		synchronized (lockObject) {
			if(topicQueue.isEmpty()){
				return null;
			}
			TopicMessage message=topicQueue.getFirst();
			MemoryTopicQueue mq=(MemoryTopicQueue) queue;
			MessagePayload payload=mq.payloadMap.get(message.payloadId);
			if(acceptSet.containsKey(message.id)){
				payload.subscriberCount--;
				if(payload.subscriberCount<=0){
					mq.payloadMap.remove(payload.id);
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
				if((System.currentTimeMillis()-message.lastDeliverTime)>queue.getMaxTtl()){
					//max ttl 
					topicQueue.removeFirst();
					payload.subscriberCount--;
					if(payload.subscriberCount<=0){
						mq.payloadMap.remove(payload.id);
					}
					logger.warn("remove message for max ttl reached:"+message.id+" "+queue.getMaxTtl());
					return MessageQueueDriver.takeNext;
				}	
			}
			//
			if((System.currentTimeMillis()-message.lastDeliverTime)<queue.getRedelieverInterval()){
				topicQueue.add(topicQueue.removeFirst());
				return MessageQueueDriver.takeNext;
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
	@Override
	public int length() {
		synchronized (lockObject) {
			return topicQueue.size();
		}
	}
	//
	public  void reject(long id){
		synchronized (lockObject) {
			super.reject(id);
			rejectSet.put(id, System.currentTimeMillis());
		}
	}
	//
	public  void accept(long id){
		synchronized (lockObject) {
			super.accept(id);
			acceptSet.put(id, System.currentTimeMillis());			
		}
	}
}
