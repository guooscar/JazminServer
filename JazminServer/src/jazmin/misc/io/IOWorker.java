/**
 * 
 */
package jazmin.misc.io;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.JazminThreadFactory;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class IOWorker implements Executor{
	private ThreadPoolExecutor poolExecutor;
	private LinkedBlockingQueue<Runnable> requestQueue;
	private LongAdder totalExecuteCount;
	private int poolSize;
	/**
	 * 
	 */
	public IOWorker(String name,int poolSize) {
		this.poolSize=poolSize;
		requestQueue=new LinkedBlockingQueue<Runnable>(1024);
		//
		poolExecutor=new ThreadPoolExecutor(
				poolSize, poolSize,
				60L,
				TimeUnit.SECONDS, 
				requestQueue, new JazminThreadFactory(name));
		poolExecutor.setRejectedExecutionHandler(
				new ThreadPoolExecutor.DiscardOldestPolicy());
		totalExecuteCount=new LongAdder();
	}
	
	@Override
	public void execute(Runnable command) {
		totalExecuteCount.add(1);
		poolExecutor.execute(command);
	}
	//
	public int getRequestQueueSize(){
		return requestQueue.size();
	}
	/**
	 * @return the corePoolSize
	 */
	public int getPoolSize() {
		return poolSize;
	}
	//
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		return poolExecutor.getActiveCount();
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getTaskCount()
	 */
	public long getTaskCount() {
		return poolExecutor.getTaskCount();
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getCompletedTaskCount()
	 */
	public long getCompletedTaskCount() {
		return poolExecutor.getCompletedTaskCount();
	}
	//
	public long getTotalExecuteCount(){
		return totalExecuteCount.longValue();
	}
}
