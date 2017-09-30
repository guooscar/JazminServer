/**
 * 
 */
package jazmin.driver.mq;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Registerable;
import jazmin.core.monitor.Monitor;
import jazmin.core.monitor.MonitorAgent;
import jazmin.core.thread.Dispatcher;
import jazmin.driver.mq.file.FileTopicQueue;
import jazmin.driver.mq.memory.MemoryTopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class MessageQueueDriver extends Driver implements Registerable{
	//
	private static Logger logger=LoggerFactory.get(MessageQueueDriver.class);
	//
	public static final String TOPIC_QUEUE_TYPE_MEMORY="memory";
	public static final String TOPIC_QUEUE_TYPE_FILE="file";
	//
	public static Message takeNext=new Message();
	//
	Map<String,TopicQueue>topicQueues;
	Map<String,TopicSubscriber>subscribers;
	Map<String,TakeThread>takeThreads;
	boolean stopTakeThread=false;
	String workDir;
	//
	//
	public MessageQueueDriver() {
		topicQueues=new ConcurrentHashMap<>();
		subscribers=new ConcurrentHashMap<>();
		takeThreads=new ConcurrentHashMap<>();
		workDir="./jazmin_mq_work";
	}
	//
	public TopicQueue createTopicQueue(String name,String type){
		if(topicQueues.containsKey(name)){
			throw new IllegalArgumentException("topic queue already exists with name "+name);
		}
		if(type.equals(TOPIC_QUEUE_TYPE_MEMORY)){
			MemoryTopicQueue queue=new MemoryTopicQueue(name);
			topicQueues.put(name,queue);
			return queue;
		}
		if(type.equals(TOPIC_QUEUE_TYPE_FILE)){
			FileTopicQueue queue=new FileTopicQueue(name);
			topicQueues.put(name,queue);
			queue.setWorkDir(workDir);
			return queue;
		}
		throw new IllegalArgumentException("bad topic type:"+type);
	}
	/**
	 * @return the workDir
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * @param workDir the workDir to set
	 */
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	//
	List<TopicQueue>getTopicQueues(){
		return new ArrayList<TopicQueue>(topicQueues.values());
	}
	//
	//
	TopicQueue getTopicQueue(String topic){
		return topicQueues.get(topic);
	}
	//
	List<TopicSubscriber>getTopicSubscribers(){
		return new ArrayList<TopicSubscriber>(subscribers.values());
	}
	//
	@Override
	public void register(Object object) {
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
			//
			if(td.id()<=0){
				throw new IllegalArgumentException("subscriber id must > 0");
			}
			//
			TopicQueue queue=topicQueues.get(td.topic());
			//
			TopicSubscriber l=new TopicSubscriber();
			l.id=td.id();
			l.method=m;
			l.topic=td.topic();
			l.instance=object;
			l.name=l.method.getName();
			if(subscribers.containsKey(l.topic+"-"+l.id)){
				throw new IllegalArgumentException("subscriber already exists with name:"+l.id+" on topic:"+l.topic);
			}
			
			subscribers.put(l.topic+"-"+l.id, l);
			queue.subscribe(l);
			logger.info("resister subscribe {} topic:{}",l.id,l.topic);
		}
	}
	//
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
	}
	//
	public void accept(Message message){
		TopicQueue queue=getQueue(message.topic);
		queue.accept(message.subscriber, message.id);
	}
	//
	public void reject(Message message){
		TopicQueue queue=getQueue(message.topic);
		queue.reject(message.subscriber, message.id);
	}
	
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		super.start();
		//
		topicQueues.forEach((k,v)->{
			v.start();
		});
		//
		subscribers.forEach((k,v)->{
			TakeThread thread=new TakeThread(v);
			takeThreads.put(k, thread);
			thread.start();
		});
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(MessageQueueDriverCommand.class);
		}
		Jazmin.mointor.registerAgent(new MessageQueueDriverMonitorAgent());
	}
	//
	@Override
	public void stop() throws Exception {
		super.stop();
		topicQueues.forEach((k,v)->{
			v.stop();
		});
		stopTakeThread=true;
	}
	//
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info").format("%-30s:%-30s\n");
		ib.print("workDir",workDir);
		ib.section("topicQueues").format("%-30s:%-30s\n");
		topicQueues.forEach((k,v)->{
			ib.print(k,v.getId()+"["+v.getType()+"]");
		});
		ib.section("subscribers").format("%-30s subscribe topic: %-30s\n");
		subscribers.forEach((k,v)->{
			ib.print(k,v.topic);
		});
		return ib.toString();
	}
	//--------------------------------------------------------------------------
	class TakeThread extends Thread{
		TopicSubscriber subscriber;
		Object lockObject=new Object();

		public TakeThread(TopicSubscriber subscriber) {
			super();
			this.subscriber=subscriber;
			setName("MessageQueueTakeThread-"+subscriber.topic+"-"+subscriber.id);
		}
		//
		@Override
		public void run() {
			while(!stopTakeThread){
				try{
					takeMessage();
				}catch(Exception e){
					logger.catching(e);
				}
			}
		}
		//
		private void takeMessage(){
			int messageCount=0;
			TopicQueue queue=topicQueues.get(subscriber.topic);
			Message message=queue.take(subscriber.id);
			if(message!=null){
				messageCount++;
				if(message!=takeNext){
					delieverMessage(subscriber,message);
				}
			}
			//
			if(messageCount==0){
				waitTake();
			}
		}
		//
		//
		void notifyTake(){
			synchronized (lockObject) {
				lockObject.notifyAll();
			}
		}
		//
		void waitTake(){
			synchronized (lockObject) {
				try {
					lockObject.wait(1000);//wait for 1 seconds
				} catch (InterruptedException e) {
					logger.catching(e);
				}
			}
		}
		//
		private void delieverMessage(TopicSubscriber subscriber,Message message){
			subscriber.delieverCount++;
			subscriber.lastDelieverTime=System.currentTimeMillis();
			MessageEvent event=new MessageEvent();
			event.message=message;
			event.messageQueueDriver=MessageQueueDriver.this;
			//
			Jazmin.dispatcher.invokeInCaller(
					"MessageQueueDriver",
					subscriber.instance,
					subscriber.method, 
					Dispatcher.EMPTY_CALLBACK,
					event);
		}
	}
	
	//--------------------------------------------------------------------------
	//
	private class MessageQueueDriverMonitorAgent implements MonitorAgent{
		@Override
		public void sample(int idx,Monitor monitor) {
			for(TopicQueue queue :getTopicQueues()){
				Map<String,String>info1=new HashMap<String, String>();
				info1.put("publishCount", queue.getPublishedCount()+"");
				monitor.sample("MessageQueueDriver.PublishCount."+queue.id,
								Monitor.CATEGORY_TYPE_COUNT,info1);
			}
			for(TopicQueue queue :getTopicQueues()){
				for(TopicChannel tc:queue.getChannels()){
					Map<String,String>info1=new HashMap<String, String>();
					info1.put("channelLength", tc.length()+"");
					monitor.sample("MessageQueueDriver.ChannelLength."+queue.id+"-"+tc.subscriber.id,
									Monitor.CATEGORY_TYPE_COUNT,info1);
				}
				
			}
		}
		//
		@Override
		public void start(Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			for(TopicQueue q:getTopicQueues()){
				info.put("TopicQueue-"+q.id, q.getType());
			}
			monitor.sample("MessageQueueDriver.Info",Monitor.CATEGORY_TYPE_KV,info);
		}
	}
	
}
