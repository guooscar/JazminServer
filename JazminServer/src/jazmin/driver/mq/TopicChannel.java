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
	//
	protected short subscriberId;
	protected Object lockObject=new Object();
	//
	protected TopicQueue queue;
	public TopicChannel(TopicQueue queue) {
		this.queue=queue;
		delieverCount=new LongAdder();
		acceptCount=new LongAdder();
		rejectCount=new LongAdder();
		expriedCount=new LongAdder();
	}
	
	/**
	 * @return the subscriberId
	 */
	public short getSubscriberId() {
		return subscriberId;
	}

	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(short subscriberId) {
		this.subscriberId = subscriberId;
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
