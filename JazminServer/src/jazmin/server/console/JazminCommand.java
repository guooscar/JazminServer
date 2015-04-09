package jazmin.server.console;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.aop.DispatcherCallback;
import jazmin.core.job.JazminJob;
import jazmin.core.task.JazminTask;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InvokeStat;
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
    	addOption("logger",false,"show all loggers",this::showLoggers);
    	addOption("loglevel",true,"set log level.ALL/DEBUG/INFO/WARN/ERROR/FATAL",this::setLogLevel);
    	addOption("task",false,"show tasks",this::showTasks);
    	addOption("job",false,"show jobs",this::showJobs);  
    	addOption("runjob",true,"run job",this::runJob);  
    	addOption("runtask",true,"run task",this::runTask);  
    	addOption("driver",false,"show all drivers",this::showDrivers);  	
    	addOption("server",false,"show all servers",this::showServers);  	
    	addOption("poolinfo",false,"show thread pool info",this::showThreadPoolInfo);  	
    	addOption("poolstat",false,"show method stats",this::showThreadPoolStats);  	
    	addOption("pooldashboard",false,"show thread pool dashboard",this::showThreadPoolDashboard); 
    	addOption("dump",false,"dump servers and drivers",this::dump); 
        
    }
    //
    private void showServerInfo(String args){
    	String format="%-20s : %-10s\n";
		out.printf(format,"name",Jazmin.getServerName());
		out.printf(format,"version",Jazmin.VERSION);
		out.printf(format,"logLevel",LoggerFactory.getLevel());
		out.printf(format,"logFile",LoggerFactory.getFile());
		out.printf(format,"startTime",formatDate(Jazmin.getStartTime()));
		out.printf(format,"bootFile",Jazmin.getBootFile());
		out.printf(format,"applicationPackage",Jazmin.getApplicationPackage());
		out.printf(format,"appClassLoader",Jazmin.getAppClassLoader());
		out.printf(format,"serverPath",Jazmin.getServerPath());	
    }
    //
    //
    private void showEnvInfo(String args){
    	String format="%-30s : %-10s\n";
    	out.format(format,"KEY","VALUE");
    	Jazmin.environment.envs().forEach((k,v)->out.format(format,k,v));
    }
    //
    private void showLoggers(String args){
		String format="%-5s: %-100s\n";
		int i=0;
		List<Logger>loggers=LoggerFactory.getLoggers();
		out.println("total "+loggers.size()+" loggers");
		out.format(format,"#","NAME");	
		for(Logger logger:loggers){
			out.format(format,i++,logger.getName());
		};
    }
    //
    private void showJobs(String args)throws Exception{
    	String format="%-5s : %-50s %-20s %-15s %-15s %-10s\n";
		int i=0;
		List<JazminJob>jobs=Jazmin.jobStore.getJobs();
		out.println("total "+jobs.size()+" jobs");
		out.format(format,"#","NAME","CRON","LAST RUN","NEXT RUN","RUNTIMES");	
		for(JazminJob job:jobs){
			out.format(format,i++,
					job.id,
					job.cron,
					formatDate(job.lastRunTime()),
					formatDate(job.nextRunTime()),
					job.runTimes);
		};
    }
    //
    private void showTasks(String args){
    	String format="%-5s : %-50s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<JazminTask>tasks=Jazmin.taskStore.getTasks();
		out.println("total "+tasks.size()+" tasks");
		out.format(format,"#","NAME","INITDELAY","PERIOD","TIMEUNIT","RUNTIMES");	
		for(JazminTask task:tasks){
			out.format(format,i++,
					task.id,
					task.initialDelay,
					task.period,
					task.unit,
					task.runTimes);
		};
    }
    private void runTask(String args){
    	Jazmin.taskStore.runTask(args);
    }
    private void runJob(String args){
    	Jazmin.jobStore.runJob(args);
    }
    //
    private void setLogLevel(String logLevel){
    	LoggerFactory.setLevel(logLevel);
    }
    //
    private void showServers(String args){
		String format="%-5s : %-100s\n";
		int i=0;
		List<Server>servers=Jazmin.getServers();
		out.println("total "+servers.size()+" servers");
		out.format(format,"#","NAME");	
		for(Server server:servers){
			out.format(format,i++,server.getClass().getSimpleName());
		};
    }
    //
    private void showDrivers(String args){
		String format="%-5s : %-100s\n";
		int i=0;
		List<Driver>drivers=Jazmin.getDrivers();
		out.println("total "+drivers.size()+" drivers");
		out.format(format,"#","NAME");	
		for(Driver driver:drivers){
			out.format(format,i++,driver.getClass().getSimpleName());
		};
    }
    //
    //
    private void showThreadPoolInfo(String args){
    	String format="%-20s : %-10s\n";
		out.printf(format,"corePoolSize",Jazmin.dispatcher.getCorePoolSize());
		out.printf(format,"maxPoolSize",Jazmin.dispatcher.getMaxPoolSize());
		int index=1;
		for(DispatcherCallback c: Jazmin.dispatcher.getGlobalDispatcherCallbacks()){
			out.printf(format,"dispatcherCallback-"+(index++),c);
		}
		//
		out.printf(format,"activeCount",Jazmin.dispatcher.getActiveCount());
		out.printf(format,"completedTaskCount",Jazmin.dispatcher.getCompletedTaskCount());
		out.printf(format,"taskCount",Jazmin.dispatcher.getTaskCount());	
    }
    //
    //
    private void showThreadPoolStats(String args){
    	String format="%-5s : %-50s %-10s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<InvokeStat>stats=Jazmin.dispatcher.getInvokeStats();
		out.println("total "+stats.size()+" method stats");
		Collections.sort(stats);
		out.format(format,"#","NAME","IVC","ERR","MINT","MAXT","AVGT");	
		for(InvokeStat stat:stats){
			out.format(format,i++,
					stat.name,
					stat.invokeCount,
					stat.errorCount,
					stat.minTime,
					stat.maxTime,
					stat.avgTime());
		};
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
