package jazmin.server.console.builtin;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.app.Application;
import jazmin.core.app.AutoWiredObject;
import jazmin.core.app.AutoWiredObject.AutoWiredField;
import jazmin.core.job.JazminJob;
import jazmin.core.task.JazminTask;
import jazmin.core.thread.PerformanceLog;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.InvokeStat;
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
    	addOption("app",false,"show app information",this::showApp);  

    	addOption("pool_info",false,"show thread pool info",this::showThreadPoolInfo);  	
    	addOption("pool_stat",false,"show method stats",this::showThreadPoolStats);  	
    	addOption("pool_log_chart",false,"show thread pool log",this::showThreadPoolLogChart); 
    	addOption("pool_log",false,"show thread pool log",this::showThreadPoolLog); 
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
    private void showApp(String args){
    	Application app=Jazmin.getApplication();
    	if(app==null){
    		out.println("can not find application");
    		return;
    	}
    	FormPrinter fp=FormPrinter.create(out,35);
    	fp.print("application",app.getClass());
    	fp.print("package",Jazmin.getApplicationPackage());
    	fp.print("classLoader",Jazmin.getAppClassLoader());
    	//
    	TablePrinter tp=TablePrinter.create(out)
    			.length(30,30,30,10,10)
    			.headers("WIREDCLASS","FIELDNAME","FIELDCLASS","SHARED","HASVALUE");
    	for(AutoWiredObject obj:app.getAutoWiredObjects()){
    		tp.printfRaw("%-38s %s\n",obj.clazz.getSimpleName(),DumpUtil.repeat("-",80));
    		for(AutoWiredField f:obj.fields){
    			tp.print("-",f.fieldName,f.fieldClass.getSimpleName(),f.shared,f.hasValue);
    		}
    	}
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
    	out.println(Jazmin.dispatcher.info());
    	FormPrinter fp=FormPrinter.create(out,35);
    	fp.print("activeCount",Jazmin.dispatcher.getActiveCount());
		fp.print("completedTaskCount",Jazmin.dispatcher.getCompletedTaskCount());
		fp.print("taskCount",Jazmin.dispatcher.getTaskCount());	
		fp.print("rejectedCount",Jazmin.dispatcher.getTotalRejectedCount());	
		double totalFull=Jazmin.dispatcher.getTotalFullTime();
		double totalRun=Jazmin.dispatcher.getTotalRunTime();
		long totalInvokeCount=Jazmin.dispatcher.getTotalInvokeCount();
		double totalCount=totalInvokeCount==0?1:totalInvokeCount;
		fp.print("avgFullTime",String.format("%.2f", totalFull/totalCount));	
		fp.print("avgRunTime",String.format("%.2f", totalRun/totalCount));	
		fp.print("maxFullTime",Jazmin.dispatcher.getMaxFullTime());	
		fp.print("maxRunTime",Jazmin.dispatcher.getMaxRunTime());	
		
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
    private void showThreadPoolLog(String args){
    	TablePrinter tp=TablePrinter.create(out).
    			length(15,10,10,10,10,10,10,10).
    			headers("TIME",
    					"POOLSIZE",
    					"QUEUESIZE",
    					"AVGFULLTIME",
    					"AVGRUNTIME",
    					"REJECTED",
    					"INVOKE",
    					"SUBMIT");
    	SimpleDateFormat sdf=new SimpleDateFormat("MM:dd HH:mm");
    	for(PerformanceLog log:Jazmin.dispatcher.getPerformanceLogs()){
        	tp.print(sdf.format(log.date),
        			log.poolSize,
        			log.queueSize,
        			log.avgFullTime,
        			log.avgRunTime,
        			log.rejectedCount,
        			log.invokeCount,
        			log.submitCount);
		};
    }
    //
    private void showThreadPoolLogChart(String args){
    	AsciiChart chartRunTime=new AsciiChart(200,80);
    	AsciiChart chartFullTime=new AsciiChart(200,80);
    	AsciiChart chartQueueSize=new AsciiChart(200,80);
    	AsciiChart chartPoolSize=new AsciiChart(200,80);
    	for(PerformanceLog log:Jazmin.dispatcher.getPerformanceLogs()){
    		chartRunTime.addValue((int)(log.avgRunTime));	
    		chartFullTime.addValue((int)(log.avgFullTime));
    		chartQueueSize.addValue(log.queueSize);
    		chartPoolSize.addValue(log.poolSize);
    	}
    	out.println("AvgRunTime");
		out.println(chartRunTime.draw());
		out.println("AvgFullTime");
		out.println(chartFullTime.draw());
		out.println("QueueSize");
		out.println(chartQueueSize.draw());
		out.println("PoolSize");
		out.println(chartPoolSize.draw());
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
