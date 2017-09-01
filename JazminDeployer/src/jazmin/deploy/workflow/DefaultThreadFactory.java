package jazmin.deploy.workflow;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
/**
 * 
 * @author yama
 *
 */
public class DefaultThreadFactory implements ThreadFactory{
	private AtomicInteger threadCounter=new AtomicInteger();
	private String threadName;
	public DefaultThreadFactory(String name) {
		threadName=name;
	}
	//
	@Override
	public Thread newThread(Runnable r) {
		Thread t=new Thread(r);
		t.setContextClassLoader(Jazmin.getAppClassLoader());
		t.setName(threadName+"-"+threadCounter.incrementAndGet());
		Thread.UncaughtExceptionHandler logHander=new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
			}
		};
		t.setUncaughtExceptionHandler(logHander);
		return t;
	}
}
