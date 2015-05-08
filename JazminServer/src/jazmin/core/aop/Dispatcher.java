/**
 * 
 */
package jazmin.core.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
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
	//
	private int corePoolSize;
	private int maxPoolSize;
	List<DispatcherCallback>globalCallbacks;
	private Map<String,InvokeStat>methodStats;
	private LongAdder totalInvokeCount;
	private LongAdder totalSubmitCount;
	/**
	 * 
	 */
	public Dispatcher() {
		corePoolSize=DEFAULT_CORE_POOL_SIZE;
		maxPoolSize=DEFAULT_MAX_POOL_SIZE;
		requestQueue=new LinkedBlockingQueue<Runnable>(1024);
		globalCallbacks=new ArrayList<DispatcherCallback>();
		methodStats=new ConcurrentHashMap<String, InvokeStat>();
		totalInvokeCount=new LongAdder();
		totalSubmitCount=new LongAdder();
		//
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		poolExecutor=new ThreadPoolExecutor(
				corePoolSize, maxPoolSize,
				60L,
				TimeUnit.SECONDS, 
				requestQueue, new JazminThreadFactory("WorkerThread"));
		poolExecutor.setRejectedExecutionHandler(
				new ThreadPoolExecutor.AbortPolicy());
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
		ib.print("corePoolSize:",corePoolSize);
		ib.print("maxPoolSize:",maxPoolSize);
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
	void statMethod(Method m,Throwable e,int time){
		String name=m.getDeclaringClass().getSimpleName()+"."+m.getName();
		InvokeStat ms=methodStats.get(name);
		if(ms==null){
			ms=new InvokeStat();
			ms.name=name;
			methodStats.put(name, ms);
		}
		ms.invoke(e!=null, time);
		totalInvokeCount.increment();
	}
	//
	public void resetInvokeStats(){
		methodStats.clear();
	}
	//
	public List<InvokeStat>getInvokeStats(){
		return new ArrayList<InvokeStat>(methodStats.values());
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
	/**
	 * @return the corePoolSize
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}
	/**
	 * @param corePoolSize the corePoolSize to set
	 */
	public void getCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	/**
	 * @return the maxPoolSize
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	/**
	 * @param maxPoolSize the maxPoolSize to set
	 */
	public void getMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
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
	public long getTotalInvokeCount(){
		return totalInvokeCount.longValue();
	}
	//
	public long getTotalSubmitCount(){
		return totalSubmitCount.longValue();
	}
	//--------------------------------------------------------------------------
	@Override
	public void execute(Runnable command) {
		poolExecutor.execute(command);
	}
}
