package jazmin.core.task;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;

/**
 * 
 * @author yama
 * 25 Dec, 2014
 */
public class JazminTask implements Runnable{
	public String id;
	public long initialDelay;
	public long period;
	public TimeUnit unit;
	public Object instance;
	public int runTimes;
	public Method method;
	public boolean runInThreadPool;
	//
	public JazminTask() {
		runTimes=0;
	}
	//
	@Override
	public void run() {
		runTimes++;
		if(runInThreadPool){
			Jazmin.dispatcher.invokeInPool("",instance, method);
		}else{
			Jazmin.dispatcher.invokeInCaller("",instance, method);
		}
	}
}
