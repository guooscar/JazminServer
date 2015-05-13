package jazmin.server.console.builtin;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.job.JazminJob;
import jazmin.core.task.JazminTask;
import jazmin.core.thread.DispatcherCallback;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.InvokeStat;
import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.FormPrinter;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.ascii.TerminalWriter;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class JazminCommand extends ConsoleCommand {
    public JazminCommand() {
    	super();
    	id="jazmin";
    	desc="jazmin server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("env",false,"show env info.",this::showEnvInfo);
    	addOption("log",false,"show all loggers",this::showLoggers);
    	addOption("log_level",true,"set log level.ALL/DEBUG/INFO/WARN/ERROR/FATAL",this::setLogLevel);
    	addOption("task",false,"show tasks",this::showTasks);
    	addOption("job",false,"show jobs",this::showJobs);  
    	addOption("job_run",true,"run job",this::runJob);  
    	addOption("task_run",true,"run task",this::runTask);  
    	addOption("driver",false,"show all drivers",this::showDrivers);  	
    	addOption("server",false,"show all servers",this::showServers);  	
    	addOption("pool_info",false,"show thread pool info",this::showThreadPoolInfo);  	
    	addOption("pool_stat",false,"show method stats",this::showThreadPoolStats);  	
    	addOption("pool_db",false,"show thread pool dashboard",this::showThreadPoolDashboard); 
    	addOption("pool_mchart",true,"show thread pool method invoke chart",this::showThreadPoolMethodChart); 
    	addOption("pool_reset",false,"reset method stats",this::resetThreadPoolStats); 
    	addOption("pool_coresize",true,"set core pool size",this::setCorePoolSize); 
    	addOption("pool_maxsize",true,"set max pool size",this::setMaxPoolSize); 
    	
    	addOption("dump",false,"dump servers and drivers",this::dump); 
        
    }
    //
    private void setMaxPoolSize(String args){
     	int count=Integer.valueOf(args);
    	Jazmin.dispatcher.setMaximumPoolSize(count);
    	out.println("max pool size set to:"+count);
    }
    //
    private void setCorePoolSize(String args){
     	int count=Integer.valueOf(args);
    	Jazmin.dispatcher.setCorePoolSize(count);
    	out.println("core pool size set to:"+count);
    	
    }
    //
    private void showServerInfo(String args){
    	FormPrinter.create(out,20).
    	print("name",Jazmin.getServerName()).
    	print("version",Jazmin.VERSION).
    	print("logLevel",LoggerFactory.getLevel()).
    	print("logFile",LoggerFactory.getFile()).
    	print("startTime",Jazmin.getStartTime()).
    	print("bootFile",Jazmin.getBootFile()).
    	print("applicationPackage",Jazmin.getApplicationPackage()).
    	print("appClassLoader",Jazmin.getAppClassLoader()).
    	print("serverPath",Jazmin.getServerPath());
    }
    //
    //
    private void showEnvInfo(String args){
    	TablePrinter tp=TablePrinter.create(out).length(30,10).headers("KEY","VALUE");
    	Jazmin.environment.envs().forEach((k,v)->{tp.print(k,v);});
    }
    //
    private void showLoggers(String args){
    	TablePrinter tp=TablePrinter.create(out).length(30).headers("NAME");
		List<Logger>loggers=LoggerFactory.getLoggers();
		for(Logger logger:loggers){
			tp.print(logger.getName());
		};
    }
    //
    private void showJobs(String args)throws Exception{
    	TablePrinter tp=TablePrinter.create(out).
    			length(40,20,15,15,10).
    			headers("NAME","CRON","LAST RUN","NEXT RUN","RUNTIMES");  	
    	List<JazminJob>jobs=Jazmin.jobStore.getJobs();
		for(JazminJob job:jobs){
			tp.print(job.id,
					job.cron,
					formatDate(job.lastRunTime()),
					formatDate(job.nextRunTime()),
					job.runTimes);
		};
    }
    //
    private void showTasks(String args){
    	TablePrinter tp=TablePrinter.create(out).
    			length(40,20,15,15,10).
    			headers("NAME","CRON","LAST RUN","NEXT RUN","RUNTIMES");  	
    	List<JazminTask>tasks=Jazmin.taskStore.getTasks();
		for(JazminTask task:tasks){
			tp.print(task.id,
					task.initialDelay,
					task.period,
					task.unit,
					task.runTimes);
		};
    }
    private void runTask(String args){
    	Jazmin.taskStore.runTask(args);
    	out.println("run task "+args+" done.");
    }
    private void runJob(String args){
    	Jazmin.jobStore.runJob(args);
    	out.println("run job "+args+" done.");
    }
    //
    private void setLogLevel(String logLevel){
    	LoggerFactory.setLevel(logLevel);
    }
    //
    private void showServers(String args){
    	TablePrinter tp=TablePrinter.create(out).length(30).headers("NAME");
    	List<Server>servers=Jazmin.getServers();
		for(Server server:servers){
			tp.print(server.getClass().getSimpleName());
		};
    }
    //
    private void showDrivers(String args){
    	TablePrinter tp=TablePrinter.create(out).length(30).headers("NAME");
		List<Driver>drivers=Jazmin.getDrivers();
		for(Driver driver:drivers){
			tp.print(driver.getClass().getSimpleName());
		};
    }
    //
    //
    private void showThreadPoolInfo(String args){
    	FormPrinter fp=FormPrinter.create(out,35);
    	fp.print("corePoolSize",Jazmin.dispatcher.getCorePoolSize());
    	fp.print("maxPoolSize",Jazmin.dispatcher.getMaximumPoolSize());
    	fp.print("keepAliveTime",Jazmin.dispatcher.getKeepAliveTime(TimeUnit.SECONDS)+" seconds");
    	fp.print("largestPoolSize",Jazmin.dispatcher.getLargestPoolSize());
    	fp.print("allowsCoreThreadTimeOut",Jazmin.dispatcher.allowsCoreThreadTimeOut());
    	fp.print("rejectedExecutionHandler",Jazmin.dispatcher.getRejectedExecutionHandler());
		
		int index=1;
		for(DispatcherCallback c: Jazmin.dispatcher.getGlobalDispatcherCallbacks()){
			fp.print("dispatcherCallback-"+(index++),c);
		}
		//
		fp.print("activeCount",Jazmin.dispatcher.getActiveCount());
		fp.print("completedTaskCount",Jazmin.dispatcher.getCompletedTaskCount());
		fp.print("taskCount",Jazmin.dispatcher.getTaskCount());	
		double totalFull=Jazmin.dispatcher.getTotalFullTime();
		double totalRun=Jazmin.dispatcher.getTotalRunTime();
		double totalCount=Jazmin.dispatcher.getTotalInvokeCount();
		fp.print("avgFullTime",String.format("%.2f", totalFull/totalCount));	
		fp.print("avgRunTime",String.format("%.2f", totalRun/totalCount));	
		
    }
    //
    private void resetThreadPoolStats(String args){
    	Jazmin.dispatcher.resetInvokeStats();
    	out.println("reset thread pool method stats done");
    }
    //
    private void showThreadPoolStats(String args){
    	TablePrinter tp=TablePrinter.create(out).
    			length(30,10,10,10,10,10,10,10,10).
    			headers("NAME","IVC","ERR","MINT-F","MAXT-F","AVGT-F","MINT-R","MAXT-R","AVGT-R");  	
    	List<InvokeStat>stats=Jazmin.dispatcher.getInvokeStats();
		Collections.sort(stats);
		for(InvokeStat stat:stats){
			tp.print(
					stat.name,
					stat.invokeCount,
					stat.errorCount,
					stat.minFullTime,
					stat.maxFullTime,
					stat.avgFullTime(),
					stat.minRunTime,
					stat.maxRunTime,
					stat.avgRunTime());
		};
    }
    //
    private void  showThreadPoolMethodChart(String args)throws Exception{
    	InvokeStat stat=Jazmin.dispatcher.getInvokeStat(args);
    	if(stat==null){
    		out.println("can not found method stat :"+args);
    		return;
    	}
    	TablePrinter tp=TablePrinter.create(out).
    	length(30,10,10,10,10,10,10,10,10).
    	headers("NAME","IVC","ERR","MINT-F","MAXT-F","AVGT-F","MINT-R","MAXT-R","AVGT-R");  	
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(200,80);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		tp.print(
    				stat.name,
    				stat.invokeCount,
    				stat.errorCount,
    				stat.minFullTime,
    				stat.maxFullTime,
    				stat.avgFullTime(),
    				stat.minRunTime,
    				stat.maxRunTime,
    				stat.avgRunTime());
    		printLine('=', 120);
    		int avgTime=stat.avgFullTime();
    		chart.addValue(avgTime);
    		out.println("method invoke avg time.current:"+avgTime+" ms");
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
    private void showThreadPoolDashboard(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(200,80);
    	lastInvokeCount=Jazmin.dispatcher.getTotalInvokeCount();
    	lastSubmitCount=Jazmin.dispatcher.getTotalSubmitCount();
    	maxInvokeTps=0;
    	maxSubmitTps=0;
    	
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		printLine('=', 100);
    		showThreadPoolInfo(args);
    		printLine('=', 100);
    		showThreadPoolTps0(chart,tw);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    	stdin.read();
    }
    //
    private long lastInvokeCount=0;
    private long lastSubmitCount=0;
    
    private long maxInvokeTps=0;
    private long maxSubmitTps=0;
    
    //
    private void showThreadPoolTps0(AsciiChart chart,TerminalWriter tw){
    	String format="%-20s : %-10s\n";
		out.printf(format,"activeCount",Jazmin.dispatcher.getActiveCount());
		out.printf(format,"completedTaskCount",Jazmin.dispatcher.getCompletedTaskCount());
		out.printf(format,"taskCount",Jazmin.dispatcher.getTaskCount());	
    	//
    	long invokeCount=Jazmin.dispatcher.getTotalInvokeCount();
    	long submitCount=Jazmin.dispatcher.getTotalSubmitCount();
    	format="%-10s %-30s %-10s %-10s %-10s %-10s %-10s\n";
    	out.printf(format,
    			"TYPE",
    			"DATE",
    			"LASTCOUNT",
    			"COUNT",
    			"MAXTPS",
    			"QUEUESIZE",
    			"TPS");
    	long invokeTps=invokeCount-lastInvokeCount;
    	if(invokeTps>maxInvokeTps){
    		maxInvokeTps=invokeTps;
    	}
    	chart.addValue((int)(invokeTps));
    	out.printf(format,
    			"INVOKE",
    			formatDate(new Date()),
    			lastInvokeCount,
    			invokeCount,
    			maxInvokeTps,
    			Jazmin.dispatcher.getRequestQueueSize(),
    			invokeTps);
    	//
    	long submitTps=submitCount-lastSubmitCount;
    	if(submitTps>maxSubmitTps){
    		maxSubmitTps=submitTps;
    	}
    	out.printf(format,
    			"SUBMIT",
    			formatDate(new Date()),
    			lastSubmitCount,
    			submitCount,
    			maxSubmitTps,
    			Jazmin.dispatcher.getRequestQueueSize(),
    			submitTps);
    	
    	lastInvokeCount=invokeCount;
    	lastSubmitCount=submitCount;
    	//
    	printLine('=', 100);
		out.println("thread pool invoke tps chart. current:"+invokeTps+"/s");
		tw.fmagenta();
		chart.reset();
		out.println(chart.draw());
		tw.reset();
		
    }
    //
    private void dump(String args){
    	Jazmin.getServers().forEach(server->{
    		out.println(server.getClass().getName()+" dump info");
    		out.println(server.info());
    	});
    	Jazmin.getDrivers().forEach(driver->{
    		out.println(driver.getClass().getName()+" dump info");
    		out.println(driver.info());
    	});
    }
}
