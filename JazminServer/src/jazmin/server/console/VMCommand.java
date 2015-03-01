package jazmin.server.console;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.util.StringUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class VMCommand extends ConsoleCommand {
    private ThreadMXBean threadMXBean;
    private RuntimeMXBean runtimeMXBean;
    private MemoryMXBean memoryMXBean;
    //
	public VMCommand() {
    	super();
    	id="vm";
    	desc="vm info";
    	addOption("runtime",false,"show runtime information.",this::showRuntimeInfo);
    	addOption("thread",false,"show thread information.",this::showThreadInfo);
    	addOption("threadtop",false,"show thread information.",this::showThreadInfoTop);
    	addOption("memory",false,"show memory information.",this::showMemoryInfo);
    	addOption("memorytop",false,"show memory information.",this::showMemoryInfoTop);
    	
    	//
    	threadMXBean=ManagementFactory.getThreadMXBean();
    	runtimeMXBean=ManagementFactory.getRuntimeMXBean();
    	memoryMXBean=ManagementFactory.getMemoryMXBean();
    }
    //
    private void showRuntimeInfo(String args)throws Exception{
    	String format="%-20s: %-10s\n";
		out.printf(format,"bootClassPath",runtimeMXBean.getBootClassPath());
		out.printf(format,"bootClassPath",runtimeMXBean.getBootClassPath());
		out.printf(format,"inputArguments",runtimeMXBean.getInputArguments());
		out.printf(format,"libraryPath",runtimeMXBean.getLibraryPath());
		out.printf(format,"managementSpecVersion",runtimeMXBean.getManagementSpecVersion());
		out.printf(format,"name",runtimeMXBean.getName());
		out.printf(format,"specName",runtimeMXBean.getSpecName());
		out.printf(format,"specVendor",runtimeMXBean.getSpecVendor());
		out.printf(format,"specVersion",runtimeMXBean.getSpecVersion());
		out.printf(format,"uptime",Duration.ofMillis(runtimeMXBean.getUptime()));
		out.printf(format,"vmName",runtimeMXBean.getVmName());
		out.printf(format,"vmVendor",runtimeMXBean.getVmVendor());
		out.printf(format,"vmVersion",runtimeMXBean.getVmVersion());
	}
   

    //
    private void showThreadInfoTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showThreadInfo(args);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    }
    //
    private void showThreadInfo(String args)throws Exception{
    	out.print("total:"+threadMXBean.getThreadCount()+" ");
    	out.print("total-started:"+threadMXBean.getTotalStartedThreadCount()+" ");
    	out.print("peak:"+threadMXBean.getPeakThreadCount()+" ");
    	out.println("deamon:"+threadMXBean.getDaemonThreadCount()+" ");
    	//
    	long threadIds[]=threadMXBean.getAllThreadIds();
    	ThreadInfo tis[]=threadMXBean.getThreadInfo(threadIds);
    	List<ThreadInfo>tList=new ArrayList<ThreadInfo>();
    	tList.addAll(Arrays.asList(tis));
    	Collections.sort(tList,(o1,o2)->o1.getThreadName().compareTo(o2.getThreadName()));
    	String format="%-5s %-50s %-20s %-10s %-10s %-10s %-10s\n";
    	out.format(format,
    			"ID",
    			"NAME",
    			"STATE",
    			"WAITCOUNT",
    			"WAITIME",
    			"BLKCOUNT",
    			"BLKTIME");
    	for(ThreadInfo ti:tList){
    		out.format(format,
    				ti.getThreadId(),
    				ti.getThreadName(),
    				ti.getThreadState(),
    				ti.getWaitedCount(),
    				ti.getWaitedTime(),
    				ti.getBlockedCount(),
    				ti.getBlockedTime());
    	}
    }
    //
    private void showMemoryInfo(String args){
    	String format="%-20s: %-10s\n";
    	MemoryUsage heapUsage=memoryMXBean.getHeapMemoryUsage();
    	MemoryUsage nonheapUsage=memoryMXBean.getNonHeapMemoryUsage();
    	
		out.printf(format,"heap.init",dumpByte(heapUsage.getInit()));
		out.printf(format,"heap.max",dumpByte(heapUsage.getMax()));
		out.printf(format,"heap.used",dumpByte(heapUsage.getUsed()));
		out.printf(format,"heap.commtted",dumpByte(heapUsage.getCommitted()));
		//
		out.printf(format,"nonheap.init",dumpByte(nonheapUsage.getInit()));
		out.printf(format,"nonheap.max",dumpByte(nonheapUsage.getMax()));
		out.printf(format,"nonheap.used",dumpByte(nonheapUsage.getUsed()));
		out.printf(format,"nonheap.commtted",dumpByte(nonheapUsage.getCommitted()));
		//
	}
    //
    //
    private void showMemoryInfoTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(160,80);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showMemoryInfo(args);
    		//
    		MemoryUsage heapUsage=memoryMXBean.getHeapMemoryUsage();
    		int heapUsagedInMB=(int)(heapUsage.getUsed()/(1024*1024));
    		chart.addValue(heapUsagedInMB);
    		out.println("-----------------------------------------------------");
    		out.println("heap used memory chart. current:"+heapUsagedInMB+" MB");
    		tw.fmagenta();
    		chart.reset();
    		out.println(chart.draw());
    		tw.reset();
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    	stdin.read();
    }
    //
    private String dumpByte(long byteCount){
    	return StringUtil.format(
    			"%20s bytes %10s KB %10s MB %10s GB", 
		    	byteCount+"",
		    	byteCount/1024+"",
		    	byteCount/(1024*1024)+"",
		    	byteCount/(1024*1024*1024)+"");
    	
    }
}
