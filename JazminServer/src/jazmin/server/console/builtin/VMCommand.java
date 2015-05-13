package jazmin.server.console.builtin;
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

import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.FormPrinter;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.ascii.TerminalWriter;
import jazmin.util.DumpUtil;
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
    	addOption("memory",false,"show memory information.",this::showMemoryInfo);
    	addOption("memorytop",false,"show memory information.",this::showMemoryInfoTop);
    	
    	//
    	threadMXBean=ManagementFactory.getThreadMXBean();
    	runtimeMXBean=ManagementFactory.getRuntimeMXBean();
    	memoryMXBean=ManagementFactory.getMemoryMXBean();
    }
    //
    private void showRuntimeInfo(String args)throws Exception{
    	FormPrinter fp=FormPrinter.create(out,20);
    	fp.print("bootClassPath",runtimeMXBean.getBootClassPath());
    	fp.print("bootClassPath",runtimeMXBean.getBootClassPath());
    	fp.print("inputArguments",runtimeMXBean.getInputArguments());
    	fp.print("libraryPath",runtimeMXBean.getLibraryPath());
    	fp.print("managementSpecVersion",runtimeMXBean.getManagementSpecVersion());
    	fp.print("name",runtimeMXBean.getName());
    	fp.print("specName",runtimeMXBean.getSpecName());
    	fp.print("specVendor",runtimeMXBean.getSpecVendor());
    	fp.print("specVersion",runtimeMXBean.getSpecVersion());
    	fp.print("uptime",Duration.ofMillis(runtimeMXBean.getUptime()));
    	fp.print("vmName",runtimeMXBean.getVmName());
    	fp.print("vmVendor",runtimeMXBean.getVmVendor());
    	fp.print("vmVersion",runtimeMXBean.getVmVersion());
	}
   

    //
    private void showThreadInfo(String args)throws Exception{
    	out.print("total:"+threadMXBean.getThreadCount()+",");
    	out.print("started:"+threadMXBean.getTotalStartedThreadCount()+",");
    	out.print("peak:"+threadMXBean.getPeakThreadCount()+",");
    	out.println("deamon:"+threadMXBean.getDaemonThreadCount()+" ");
    	//
    	long threadIds[]=threadMXBean.getAllThreadIds();
    	ThreadInfo tis[]=threadMXBean.getThreadInfo(threadIds);
    	List<ThreadInfo>tList=new ArrayList<ThreadInfo>();
    	tList.addAll(Arrays.asList(tis));
    	Collections.sort(tList,(o1,o2)->o1.getThreadName().compareTo(o2.getThreadName()));
    	
    	TablePrinter tp=TablePrinter.create(out).
    			length(5,50,20,10,10,10,10).
    			headers("ID",
    			"NAME",
    			"STATE",
    			"WAITCOUNT",
    			"WAITIME",
    			"BLKCOUNT",
    			"BLKTIME");
    	
    	for(ThreadInfo ti:tList){
    		tp.print(
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
    	FormPrinter fp=FormPrinter.create(out,20);
    	MemoryUsage heapUsage=memoryMXBean.getHeapMemoryUsage();
    	MemoryUsage nonheapUsage=memoryMXBean.getNonHeapMemoryUsage();
    	
    	fp.print("heap.init",dumpByte(heapUsage.getInit()));
    	fp.print("heap.max",dumpByte(heapUsage.getMax()));
    	fp.print("heap.used",dumpByte(heapUsage.getUsed()));
    	fp.print("heap.commtted",dumpByte(heapUsage.getCommitted()));
		//
    	fp.print("nonheap.init",dumpByte(nonheapUsage.getInit()));
    	fp.print("nonheap.max",dumpByte(nonheapUsage.getMax()));
    	fp.print("nonheap.used",dumpByte(nonheapUsage.getUsed()));
    	fp.print("nonheap.commtted",dumpByte(nonheapUsage.getCommitted()));
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
    	return DumpUtil.byteCountToString(byteCount);
    }
}
