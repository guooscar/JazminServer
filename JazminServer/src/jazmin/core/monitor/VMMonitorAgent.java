/**
 * 
 */
package jazmin.core.monitor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 * 9 Jun, 2016
 */
public class VMMonitorAgent implements MonitorAgent{
	private ThreadMXBean threadMXBean;
	private RuntimeMXBean runtimeMXBean;
	private MemoryMXBean memoryMXBean;
	private GarbageCollectorMXBean garbageCollectorMXBean;
	//
	@Override
	public void start(Monitor monitor) {
		threadMXBean=ManagementFactory.getThreadMXBean();
    	runtimeMXBean=ManagementFactory.getRuntimeMXBean();
    	memoryMXBean=ManagementFactory.getMemoryMXBean();
    	garbageCollectorMXBean=ManagementFactory.getGarbageCollectorMXBeans().get(0);
    	
		//
		Map<String,String>vmInfo=new HashMap<String, String>();
		vmInfo.put("bootClassPath",runtimeMXBean.getBootClassPath());
		vmInfo.put("bootClassPath",runtimeMXBean.getBootClassPath());
		vmInfo.put("inputArguments",runtimeMXBean.getInputArguments().toString());
		vmInfo.put("libraryPath",runtimeMXBean.getLibraryPath());
		vmInfo.put("managementSpecVersion",runtimeMXBean.getManagementSpecVersion());
		vmInfo.put("name",runtimeMXBean.getName());
		vmInfo.put("specName",runtimeMXBean.getSpecName());
		vmInfo.put("specVendor",runtimeMXBean.getSpecVendor());
		vmInfo.put("specVersion",runtimeMXBean.getSpecVersion());
		vmInfo.put("uptime",Duration.ofMillis(runtimeMXBean.getUptime()).toString());
		vmInfo.put("vmName",runtimeMXBean.getVmName());
		vmInfo.put("vmVendor",runtimeMXBean.getVmVendor());
		vmInfo.put("vmVersion",runtimeMXBean.getVmVersion());
		//
		monitor.sample("VM.Info",Monitor.CATEGORY_TYPE_KV,vmInfo);
	}

	@Override
	public void sample(int idx,Monitor monitor) {
		Map<String,String>threadInfo=new HashMap<String, String>();
		threadInfo.put("total",threadMXBean.getThreadCount()+"");
		threadInfo.put("peak",threadMXBean.getPeakThreadCount()+"");
		threadInfo.put("deamon",threadMXBean.getDaemonThreadCount()+"");
		monitor.sample("VM.Thread",Monitor.CATEGORY_TYPE_VALUE,threadInfo);
		
		
		Map<String,String>threadStartedInfo=new HashMap<String, String>();
		threadStartedInfo.put("started",threadMXBean.getTotalStartedThreadCount()+"");
		monitor.sample("VM.ThreadStarted",Monitor.CATEGORY_TYPE_COUNT,threadStartedInfo);
		//	
		Map<String,String>memoryInfo=new HashMap<String, String>();
		MemoryUsage heapUsage=memoryMXBean.getHeapMemoryUsage();
    	MemoryUsage nonheapUsage=memoryMXBean.getNonHeapMemoryUsage();
    	memoryInfo.put("heap.init",(heapUsage.getInit())+"");
    	memoryInfo.put("heap.max",(heapUsage.getMax())+"");
    	memoryInfo.put("heap.used",(heapUsage.getUsed())+"");
		memoryInfo.put("heap.commtted",(heapUsage.getCommitted())+"");
		monitor.sample("VM.MemoryHeap",Monitor.CATEGORY_TYPE_VALUE,memoryInfo);
		
		//
		Map<String,String>memory2Info=new HashMap<String, String>();
		
		memory2Info.put("nonheap.init",(nonheapUsage.getInit())+"");
		memory2Info.put("nonheap.max",(nonheapUsage.getMax())+"");
		memory2Info.put("nonheap.used",(nonheapUsage.getUsed())+"");
		memory2Info.put("nonheap.commtted",(nonheapUsage.getCommitted())+"");
		monitor.sample("VM.MemoryNonHeap",Monitor.CATEGORY_TYPE_VALUE,memory2Info);
		//
		Map<String,String>garInfo1=new HashMap<String, String>();
		garInfo1.put("collectionCount",(garbageCollectorMXBean.getCollectionCount())+"");
		monitor.sample("VM.GarbageCollectorCount",Monitor.CATEGORY_TYPE_COUNT,garInfo1);
		//
		Map<String,String>garInfo2=new HashMap<String, String>();
		garInfo2.put("collectionCount",(garbageCollectorMXBean.getCollectionTime())+"");
		monitor.sample("VM.GarbageCollectorTime",Monitor.CATEGORY_TYPE_COUNT,garInfo2);
		
	}
	
}
