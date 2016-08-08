/**
 * 
 */
package jazmin.core.monitor;

import java.util.HashMap;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.core.thread.Dispatcher;

/**
 * @author yama
 * 9 Jun, 2016
 */
public class DispatcherMonitorAgent implements MonitorAgent{
	//
	@Override
	public void start(Monitor monitor) {
		Dispatcher dispatcher=Jazmin.dispatcher;
		Map<String,String>info=new HashMap<String, String>();
		info.put("corePoolSize",dispatcher.getCorePoolSize()+"");
		info.put("maxPoolSize",dispatcher.getMaximumPoolSize()+"");
		info.put("rejectedExecutionHandler",dispatcher.getRejectedExecutionHandler()+"");
		info.put("availableProcessors",Runtime.getRuntime().availableProcessors()+"");
		info.put("performanceLogFile",dispatcher.getPerformanceLogFile()+"");
		info.put("largestPoolSize",dispatcher.getLargestPoolSize()+"");
		info.put("allowsCoreThreadTimeOut",dispatcher.allowsCoreThreadTimeOut()+"");
		monitor.sample("Jazmin.Dispatcher.Info",Monitor.CATEGORY_TYPE_KV,info);
	}

	@Override
	public void sample(int idx,Monitor monitor) {
		Map<String,String>poolSize=new HashMap<String, String>();
		Map<String,String>queueSize=new HashMap<String, String>();
		Map<String,String>avgTime=new HashMap<String, String>();
		Map<String,String>invokeCnt=new HashMap<String, String>();
		
		Dispatcher dispatcher=Jazmin.dispatcher;
		double totalFullTime=dispatcher.getTotalFullTime();
		double totalRunTime=dispatcher.getTotalRunTime();
		double totalInvokeCount=dispatcher.getTotalInvokeCount();
		if(totalInvokeCount<=0){
			totalInvokeCount=1;
		}
		poolSize.put("currentSize",dispatcher.getPoolSize()+"");
		poolSize.put("maxPoolSize",dispatcher.getMaximumPoolSize()+"");
		//
		queueSize.put("queueSize",dispatcher.getQueue().size()+"");
		//
		avgTime.put("avgFullTime",(totalFullTime/totalInvokeCount)+"");
		avgTime.put("avgRunTime",(totalRunTime/totalInvokeCount)+"");
		//
		invokeCnt.put("invokeCount",dispatcher.getTotalInvokeCount()+"");
		monitor.sample("Jazmin.Dispatcher.PoolSize",Monitor.CATEGORY_TYPE_VALUE,poolSize);
		monitor.sample("Jazmin.Dispatcher.QueueSize",Monitor.CATEGORY_TYPE_VALUE,queueSize);
		monitor.sample("Jazmin.Dispatcher.AvgTime",Monitor.CATEGORY_TYPE_VALUE,avgTime);
		monitor.sample("Jazmin.Dispatcher.InvokeCount",Monitor.CATEGORY_TYPE_COUNT,invokeCnt);
	}
	
}
