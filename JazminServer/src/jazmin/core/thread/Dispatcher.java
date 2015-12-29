/**
 * 
 */
package jazmin.core.thread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Jazmin;
import jazmin.core.JazminThreadFactory;
import jazmin.core.Lifecycle;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.InvokeStat;

/**
 * <pre>
 * NOTE by default INFO logger level will print all invoke trace to file and console,
 * console output will cost a lot time. REMEBER to close console log via 
 * LoggerFactory.disableConsoleLog(); or BootContext.disableConsoleLog();
 * 
 * N(threads)=N(cpu)*U(cpu)*(1+w/c);
 * N(threads)是最后得到的结果大小 。
 * N(cpu)是cpu数量，我的电脑是双核四线程，cpu的数量会是4，可以通过        
 * System.out.println(Runtime.getRuntime().availableProcessors());来得到cpu数量
 * U(cpu)是目标cpu使用率，取决于程序员的期望，一般在50%左右。这个值限制在0到1之间
 * w/c是wait time/compute time的比率。
 * </pre>
 * @author yama
 * 23 Dec, 2014
 */
public class Dispatcher extends Lifecycle implements Executor{
	private static Logger logger=LoggerFactory.get(Dispatcher.class);
	//
	public static final Object EMPTY_ARGS[]=new Object[]{};
	public static final DispatcherCallback EMPTY_CALLBACK=new DispatcherCallbackAdapter(){};
	//
	private static final int DEFAULT_CORE_POOL_SIZE=32;
	private static final int DEFAULT_MAX_POOL_SIZE=64;
	//
	private ThreadPoolExecutor poolExecutor;
	private LinkedBlockingQueue<Runnable> requestQueue;
	private String performanceLogFile;
	//
	List<DispatcherCallback>globalCallbacks;
	private Map<String,InvokeStat>methodStats;
	private LongAdder totalInvokeCount;
	private LongAdder totalSubmitCount;
	private LongAdder totalRunTime;
	private LongAdder totalFullTime;
	private LongAdder totalRejectedCount;
	private AtomicLong maxFullTime;
	private AtomicLong maxRunTime;
	private LinkedList<PerformanceLog>performanceLogs;
	//
	/**
	 * 
	 */
	public Dispatcher() {
		requestQueue=new LinkedBlockingQueue<Runnable>(20480);
		globalCallbacks=new ArrayList<DispatcherCallback>();
		methodStats=new ConcurrentHashMap<String, InvokeStat>();
		totalInvokeCount=new LongAdder();
		totalSubmitCount=new LongAdder();
		totalRunTime=new LongAdder();
		totalFullTime=new LongAdder();
		maxFullTime=new AtomicLong();
		maxRunTime=new AtomicLong();
		totalRejectedCount=new LongAdder();
		poolExecutor=new ThreadPoolExecutor(
				DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE,
				60,
				TimeUnit.SECONDS, 
				requestQueue, new JazminThreadFactory("WorkerThread"));

		poolExecutor.setRejectedExecutionHandler(
				new ThreadPoolExecutor.AbortPolicy());
		performanceLogs=new LinkedList<PerformanceLog>();
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		new PerformanceLogWriter(performanceLogFile, this).start();
	}
	
	//--------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T>clazz,T object){
		if(!clazz.isInterface()){
			throw new IllegalArgumentException("target class must be interface.");
		}
		InvocationHandler handler=new DispatcherInvocationHandler(object,this);
		Object proxyObject=Proxy.newProxyInstance(
				clazz.getClassLoader(),
				new Class<?>[]{clazz}, 
				handler);
		return (T) proxyObject;
	}
	//--------------------------------------------------------------------------
	//
	public String getPerformanceLogFile() {
		return performanceLogFile;
	}
	//
	public void setPerformanceLogFile(String performanceLogFile) {
		this.performanceLogFile = performanceLogFile;
	}
	//
	public PerformanceLog addPerformanceLog(){
		double totalFullTime=getTotalFullTime();
		double totalRunTime=getTotalRunTime();
		double totalInvokeCount=getTotalInvokeCount();
		if(totalInvokeCount<1){
			totalInvokeCount=1;
		}
		PerformanceLog log=new PerformanceLog();
		log.date=new Date();
		log.poolSize=getPoolSize();
		log.queueSize=getQueue().size();
		log.avgFullTime=totalFullTime/totalInvokeCount;
		log.avgRunTime=totalRunTime/totalInvokeCount;
		log.rejectedCount=getTotalRejectedCount();
		log.invokeCount=getTotalInvokeCount();
		log.submitCount=getTotalSubmitCount();
		synchronized (performanceLogs) {
			performanceLogs.add(log);
			if(performanceLogs.size()>60*24){
				//only record one day
				performanceLogs.removeFirst();
			}
		}	
		return log;
	}
	//
	public List<PerformanceLog>getPerformanceLogs(){
		synchronized (performanceLogs) {
			return new ArrayList<PerformanceLog>(performanceLogs);
		}
	}
	//
	/**
	 * 
	 */
	@Override
	public void stop(){
		long rejectTaskCount=poolExecutor.shutdownNow().size();
		logger.info("reject task count:{}",rejectTaskCount);
	}

	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create().format("%-30s:%-30s\n");
		ib.print("corePoolSize:",getCorePoolSize());
		ib.print("maxPoolSize:",getMaximumPoolSize());
		ib.print("rejectedExecutionHandler:",getRejectedExecutionHandler());
		ib.print("availableProcessors:",Runtime.getRuntime().availableProcessors());
		ib.print("performanceLogFile:",getPerformanceLogFile());
		ib.print("keepAliveTime",getKeepAliveTime(TimeUnit.SECONDS)+" seconds");
		ib.print("largestPoolSize",Jazmin.dispatcher.getLargestPoolSize());
		ib.print("allowsCoreThreadTimeOut",allowsCoreThreadTimeOut());
    	ib.section("global callbacks");
		globalCallbacks.forEach(ib::println);
		return ib.toString();
	}
	//--------------------------------------------------------------------------
	//
	public int getRequestQueueSize(){
		return requestQueue.size();
	}
	//
	void statMethod(Method m,Throwable e,int runTime,int fullTime){
		String name=m.getDeclaringClass().getSimpleName()+"."+m.getName();
		InvokeStat ms=methodStats.get(name);
		if(ms==null){
			ms=new InvokeStat();
			ms.name=name;
			methodStats.put(name, ms);
		}
		ms.invoke(e!=null, runTime,fullTime);
		totalInvokeCount.increment();
		totalFullTime.add(fullTime);
		totalRunTime.add(runTime);
		if(maxFullTime.intValue()<fullTime){
			maxFullTime.set(fullTime);
		}
		if(maxRunTime.intValue()<runTime){
			maxRunTime.set(runTime);
		}
		//
		if(totalInvokeCount.longValue()<0){
			totalInvokeCount.reset();
		}
		if(totalFullTime.longValue()<0){
			totalFullTime.reset();
		}
		if(totalRunTime.longValue()<0){
			totalRunTime.reset();
		}
	}
	//
	public long getTotalRejectedCount(){
		return totalRejectedCount.longValue();
	}
	/**
	 * @return the maxFullTime
	 */
	public long getMaxFullTime() {
		return maxFullTime.longValue();
	}
	/**
	 * @return the maxRunTime
	 */
	public long getMaxRunTime() {
		return maxRunTime.longValue();
	}
	/**
	 * @return the totalRunTime
	 */
	public long getTotalRunTime() {
		return totalRunTime.longValue();
	}
	/**
	 * @return the totalFullTime
	 */
	public long getTotalFullTime() {
		return totalFullTime.longValue();
	}
	//
	public void resetInvokeStats(){
		methodStats.clear();
		totalFullTime.reset();
		totalInvokeCount.reset();
		totalSubmitCount.reset();
		totalRunTime.reset();
	}
	//
	public List<InvokeStat>getInvokeStats(){
		return new ArrayList<InvokeStat>(methodStats.values());
	}
	//
	public InvokeStat getInvokeStat(String callId){
		return methodStats.get(callId);
	}
	//
	public void addGlobalDispatcherCallback(DispatcherCallback callback){
		globalCallbacks.add(callback);
	}
	//
	public List<DispatcherCallback> getGlobalDispatcherCallbacks(){
		return new ArrayList<DispatcherCallback>(globalCallbacks);
	}
	//
	public ThreadWorker invokeInCaller(
			String traceId,
			Object instance,
			Method method,
			DispatcherCallback callback,
			Object ...args){
		totalSubmitCount.increment();
		ThreadWorker tw=new ThreadWorker(
				this,
				traceId,
				instance, method, args,callback);
		tw.run();
		return tw;
	}
	//
	public void invokeInCaller(Invoke invoke){
		try {
			Method method=invoke.getClass().getMethod("doInvoke");
			invokeInCaller(invoke.getClass().getSimpleName(),invoke,method
					,EMPTY_CALLBACK,EMPTY_ARGS);
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	//
	/**
	 * 
	 */
	public void invokeInPool(
			String traceId,
			Object instance,
			Method method,
			DispatcherCallback callback,
			Object ...args){
		totalSubmitCount.increment();
		try{
			poolExecutor.execute(new ThreadWorker(
					this,
					traceId,
					instance, method, args,callback));
		}catch(RejectedExecutionException e){
			totalRejectedCount.increment();
			logger.error("task rejected {}-{}.{},queueSize:{}",
					traceId,
					instance.getClass().getSimpleName(),
					method.getName(),
					getQueue().size());
		}catch (Throwable e) {
			logger.catching(e);
		}
	}
	//
	public void invokeInPool(Invoke invoke){
		try {
			Method method=invoke.getClass().getMethod("doInvoke");
			invokeInPool(invoke.getClass().getSimpleName(),invoke,method);
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	//
	/**
	 * 
	 */
	public void invokeInPool(String traceId,Object instance,Method method){
		invokeInPool(
				traceId,
				instance, 
				method, 
				EMPTY_CALLBACK,
				EMPTY_ARGS);
	}
	/**
	 *
	 */
	public static Method getMethod(Class<?>clazz,String name,Class<?>...pTypes){
		try {
			return clazz.getDeclaredMethod(name, pTypes);
		} catch (Exception e) {
			return null;
		} 
	}
	//--------------------------------------------------------------------------
	
	//
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		return poolExecutor.getActiveCount();
	}
	/**
	 * @param corePoolSize
	 * @see java.util.concurrent.ThreadPoolExecutor#setCorePoolSize(int)
	 */
	public void setCorePoolSize(int corePoolSize) {
		poolExecutor.setCorePoolSize(corePoolSize);
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getCorePoolSize()
	 */
	public int getCorePoolSize() {
		return poolExecutor.getCorePoolSize();
	}
	/**
	 * @param maximumPoolSize
	 * @see java.util.concurrent.ThreadPoolExecutor#setMaximumPoolSize(int)
	 */
	public void setMaximumPoolSize(int maximumPoolSize) {
		poolExecutor.setMaximumPoolSize(maximumPoolSize);
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getMaximumPoolSize()
	 */
	public int getMaximumPoolSize() {
		return poolExecutor.getMaximumPoolSize();
	}
	/**
	 * @param time
	 * @param unit
	 * @see java.util.concurrent.ThreadPoolExecutor#setKeepAliveTime(long, java.util.concurrent.TimeUnit)
	 */
	public void setKeepAliveTime(long time, TimeUnit unit) {
		poolExecutor.setKeepAliveTime(time, unit);
	}
	/**
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getKeepAliveTime(java.util.concurrent.TimeUnit)
	 */
	public long getKeepAliveTime(TimeUnit unit) {
		return poolExecutor.getKeepAliveTime(unit);
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getQueue()
	 */
	public BlockingQueue<Runnable> getQueue() {
		return poolExecutor.getQueue();
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
	 */
	public int getPoolSize() {
		return poolExecutor.getPoolSize();
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
	public long getTotalInvokeCount(){
		return totalInvokeCount.longValue();
	}
	//
	public long getTotalSubmitCount(){
		return totalSubmitCount.longValue();
	}
	
	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
		poolExecutor.setRejectedExecutionHandler(handler);
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getRejectedExecutionHandler()
	 */
	public RejectedExecutionHandler getRejectedExecutionHandler() {
		return poolExecutor.getRejectedExecutionHandler();
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#allowsCoreThreadTimeOut()
	 */
	public boolean allowsCoreThreadTimeOut() {
		return poolExecutor.allowsCoreThreadTimeOut();
	}
	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getLargestPoolSize()
	 */
	public int getLargestPoolSize() {
		return poolExecutor.getLargestPoolSize();
	}
	//--------------------------------------------------------------------------
	@Override
	public void execute(Runnable command) {
		poolExecutor.execute(command);
	}
}
