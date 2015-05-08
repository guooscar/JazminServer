/**
 * 
 */
package jazmin.core.thread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;

/**
 * @author yama 23 Dec, 2014
 */
public class ThreadWorker implements Runnable {
	private static Logger logger = LoggerFactory.get(ThreadWorker.class);
	//
	private Object instance;
	private Object[] args;
	private Method method;
	private DispatcherCallback callback;
	private String traceId;
	private Dispatcher dispatcher;
	private List<DispatcherCallback>globalDispatcherCallbacks;
	private Object ret = null;
	private Throwable exception = null;
	private long startTime;
	//
	public ThreadWorker(
			Dispatcher dispatcher,
			String traceId,
			Object instance,
			Method method, 
			Object[] args,
			DispatcherCallback callback) {
		if(instance==null){
			throw new IllegalArgumentException("instance can not be null");
		}
		if(method==null){
			throw new IllegalArgumentException("method can not be null");
		}
		this.dispatcher=dispatcher;
		if(dispatcher.globalCallbacks.isEmpty()){
			this.globalDispatcherCallbacks=null;
		}else{
			globalDispatcherCallbacks=dispatcher.globalCallbacks;
		}
		this.traceId=traceId;
		this.instance = instance;
		this.args = args;
		this.method = method;
		this.callback = callback;
		startTime = System.currentTimeMillis();
	}
	//
	@Override
	public void run() {
		String oldName=Thread.currentThread().getName();
		Thread.currentThread().setName(oldName+"-"+traceId);
		long runStartTime=System.currentTimeMillis();;
		String methodName=method.getDeclaringClass().getSimpleName() + "."+ method.getName();
		if (logger.isInfoEnabled()) {
			logger.info(">invoke:-{}",methodName);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(DumpUtil.dumpInvokeArgs(">invoke:" + methodName, args));
		}
		try {
			if(globalDispatcherCallbacks!=null){
				for(DispatcherCallback c:globalDispatcherCallbacks){
					c.before(methodName, method, args);
				}
			}
			callback.before(methodName, method, args);
			ret = method.invoke(instance, args);
			callback.after(instance, method,args);
			//
			if(globalDispatcherCallbacks!=null){
				for(DispatcherCallback c:globalDispatcherCallbacks){
					c.after(methodName, method, args);
				}
			}
		} catch (InvocationTargetException e) {
			exception = e.getTargetException();
		} catch (Throwable e) {
			exception = e;
		} finally {
			long current=System.currentTimeMillis();
			long fullTime=current-startTime;
			long runTime=current-runStartTime;
			if (exception != null) {
				if(exception instanceof AppException){
					AppException ae=(AppException)exception;
					logger.warn("<invoke:{},app exception code={},msg={}" ,
							methodName,
							ae.getCode(),
							ae.getMessage());
				}else{
					logger.error("<invoke:" + methodName, exception);		
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("<invoke:{} time:{}-{}", methodName,
						runTime,fullTime);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(DumpUtil.dumpInvokeObject("<invoke:" + methodName,ret));
			}
			if(globalDispatcherCallbacks!=null){
				for(DispatcherCallback c:globalDispatcherCallbacks){
					c.end(instance, method,args,ret,exception);
				}
			}
			callback.end(instance, method,args,ret,exception);
			dispatcher.statMethod(method, exception,(int) runTime,(int) fullTime);
			Thread.currentThread().setName(oldName);
		}
	}
	//

	/**
	 * @return the ret
	 */
	public Object getRet() {
		return ret;
	}

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}
	
}
