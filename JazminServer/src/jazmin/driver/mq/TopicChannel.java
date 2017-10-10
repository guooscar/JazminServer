package jazmin.driver.mq;

import java.util.concurrent.atomic.LongAdder;

/**
 * 
 * @author yama
 *
 */
public abstract class TopicChannel {
	protected LongAdder delieverCount;
	protected LongAdder acceptCount;
	protected LongAdder rejectCount;
	protected LongAdder expriedCount;
	protected Object lockObject=new Object();
	protected TopicQueue queue;
	protected TopicSubscriber subscriber;
	//
	public TopicChannel(TopicQueue queue,TopicSubscriber subscriber) {
		this.queue=queue;
		this.subscriber=subscriber;
		delieverCount=new LongAdder();
		acceptCount=new LongAdder();
		rejectCount=new LongAdder();
		expriedCount=new LongAdder();
	}
	
	public TopicSubscriber getSubscriber() {
		return subscriber;
	}
	/**
	 * 
	 * @param subscriber
	 * @return
	 */
	public abstract Message take();
	public abstract int length();
	//
	public void stop(){
		
	}
	//
	public  void reject(long id){
		rejectCount.increment();
	}
	//
	public  void accept(long id){
		acceptCount.increment();
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
}
