/**
 * 
 */
package jazmin.core.thread;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.JazminThreadFactory;
import jazmin.core.Lifecycle;
import jazmin.driver.rpc.ProxyInvocationHandler;
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
	private static final int DEFAULT_CORE_POOL_SIZE=16;
	private static final int DEFAULT_MAX_POOL_SIZE=64;
	//
	private ThreadPoolExecutor poolExecutor;
	private LinkedBlockingQueue<Runnable> requestQueue;
	//
	List<DispatcherCallback>globalCallbacks;
	private Map<String,InvokeStat>methodStats;
	private LongAdder totalInvokeCount;
	private LongAdder totalSubmitCount;
	private LongAdder totalRunTime;
	private LongAdder totalFullTime;
	/**
	 * 
	 */
	public Dispatcher() {
		requestQueue=new LinkedBlockingQueue<Runnable>(10240);
		globalCallbacks=new ArrayList<DispatcherCallback>();
		methodStats=new ConcurrentHashMap<String, InvokeStat>();
		totalInvokeCount=new LongAdder();
		totalSubmitCount=new LongAdder();
		totalRunTime=new LongAdder();
		totalFullTime=new LongAdder();
		poolExecutor=new ThreadPoolExecutor(
				DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE,
				60,
				TimeUnit.SECONDS, 
				requestQueue, new JazminThreadFactory("WorkerThread"));
		poolExecutor.setRejectedExecutionHandler(
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
	
	}
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
		ib.section("global callbacks");
		globalCallbacks.forEach(ib::println);
		return ib.toString();
	}
	
	//--------------------------------------------------------------------------
	@SuppressWarnings("unchecked")	
	public <T> T createProxy(Class<T>clazz,T obj){
		if(!clazz.isInterface()){
			throw new IllegalArgumentException("target class must be interface.");
		}
		//
		ProxyInvocationHandler invocationHandler=new ProxyInvocationHandler(this,obj);
		T proxyObject=(T) Proxy.newProxyInstance(
				clazz.getClassLoader(),
				new Class<?>[]{clazz}, 
				invocationHandler);
		return proxyObject;
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
		poolExecutor.execute(new ThreadWorker(
				this,
				traceId,
				instance, method, args,callback));
	}
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
