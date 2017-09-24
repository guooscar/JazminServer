/**
 * 
 */
package jazmin.driver.mq;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.thread.Dispatcher;
import jazmin.driver.mq.memory.MemoryTopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class MessageQueueDriver extends Driver{
	//
	private static Logger logger=LoggerFactory.get(MessageQueueDriver.class);
	//
	public static final String TOPIC_QUEUE_TYPE_MEMORY="memory";
	//
	Map<String,TopicQueue>topicQueues;
	Map<String,TopicSubscriber>subscribers;
	Thread takeMessageThread;
	boolean stopTakeThread=false;
	//
	private Object lockObject=new Object();
	//
	public MessageQueueDriver() {
		topicQueues=new ConcurrentHashMap<>();
		subscribers=new ConcurrentHashMap<>();
		//
		
	}
	//
	public void createTopic(String name,String type){
		if(topicQueues.containsKey(name)){
			throw new IllegalArgumentException("topic queue already exists with name "+name);
		}
		if(type.equals(TOPIC_QUEUE_TYPE_MEMORY)){
			topicQueues.put(name,new MemoryTopicQueue(name));
			return;
		}
		throw new IllegalArgumentException("bad topic type:"+type);
	}
	//
	public List<TopicQueue>getTopicQueues(){
		return new ArrayList<TopicQueue>(topicQueues.values());
	}
	//
	//
	public List<TopicSubscriber>getTopicSubscribers(){
		return new ArrayList<TopicSubscriber>(subscribers.values());
	}
	/**
	 * 
	 * @param object
	 */
	public void subscribe(Object object){
		if(isStarted()){
			throw new IllegalStateException("register before started.");
		}
		for(Method m:object.getClass().getDeclaredMethods()){
			if(!m.isAnnotationPresent(TopicSubscriberDefine.class)){
				continue;
			}
			if(!Modifier.isPublic(m.getModifiers())){
				throw new IllegalArgumentException("subscriber method shoule be public");
			}
			if(m.getParameterCount()!=1){
				throw new IllegalArgumentException("subscriber method parameter must be MessageEvent");
			}
			if(!m.getParameters()[0].getType().equals(MessageEvent.class)){
				throw new IllegalArgumentException("subscriber method parameter must be MessageEvent");
			}
			TopicSubscriberDefine td= m.getAnnotation(TopicSubscriberDefine.class);
			//
			if(!topicQueues.containsKey(td.topic())){
				throw new IllegalArgumentException("can not find topic queue with name:"+td.topic());
			}
			TopicQueue queue=topicQueues.get(td.topic());
			//
			TopicSubscriber l=new TopicSubscriber();
			l.id=m.getDeclaringClass().getSimpleName()+"."+m.getName();
			l.method=m;
			l.topic=td.topic();
			l.instance=object;
			subscribers.put(l.id, l);
			queue.subscribe(l.id);
			logger.info("resister subscribe {} topic:{}",l.id,l.topic);
		}
	}
	//
	private TopicQueue getQueue(String topic){
		TopicQueue queue=topicQueues.get(topic);
		if(queue==null){
			throw new IllegalArgumentException("can not find topic queue:"+topic);
		}
		return queue;
	}
	//
	public void publish(String topic,Object payload){
		getQueue(topic).publish(payload);
		notifyTake();
	}
	//
	public void accept(String topic,String messageId){
		getQueue(topic).accept(messageId);
		notifyTake();
	}
	//
	public void reject(String topic,String messageId){
		getQueue(topic).reject(messageId);
		notifyTake();
	}
	//
	private void notifyTake(){
		synchronized (lockObject) {
			lockObject.notifyAll();
		}
	}
	//
	private void waitTake(){
		synchronized (lockObject) {
			try {
				lockObject.wait();
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}
	//
	public void takeMessage(){
		while(!stopTakeThread){
			int messageCount=0;
			for(Entry<String,TopicQueue>e : topicQueues.entrySet()){
				Message message=e.getValue().take();
				if(message!=null){
					messageCount++;
					if(message.id!=null){
						sendMessage(message);
					}
				}
			}
			//
			if(messageCount==0){
				waitTake();
			}
		}
	}
	//
	private void sendMessage(Message message){
		TopicSubscriber subscriber=subscribers.get(message.subscriber);
		subscriber.sentCount++;
		subscriber.lastSentTime=System.currentTimeMillis();
		MessageEvent event=new MessageEvent();
		event.message=message;
		event.messageQueueDriver=this;
		Jazmin.dispatcher.invokeInPool(
				"MessageQueueDriver",
				subscriber.instance,
				subscriber.method, Dispatcher.EMPTY_CALLBACK,
				event);
	}
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		super.start();
		takeMessageThread=new Thread(this::takeMessage);
		takeMessageThread.setName("MessageQueueDriverTakeThread");
		takeMessageThread.start();
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(MessageQueueDriverCommand.class);
		}
	}
	//
	@Override
	public void stop() throws Exception {
		super.stop();
		stopTakeThread=true;
	}
	//
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("topicQueues").format("%-30s:%-30s\n");
		topicQueues.forEach((k,v)->{
			ib.print(k,v);
		});
		ib.section("subscribers").format("%-30s subscribe topic: %-30s\n");
		subscribers.forEach((k,v)->{
			ib.print(k,v.topic);
		});
		return ib.toString();
	}
	
}
