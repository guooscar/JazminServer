package jazmin.server.rpc;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Jazmin;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.TerminalWriter;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RpcServerCommand extends ConsoleCommand {
    private RpcServer rpcServer;
	public RpcServerCommand() {
    	super();
    	id="rpcsrv";
    	desc="rpc server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("srv",false,"show services.",this::showServices);
    	addOption("s",false,"show all sessions.",this::showSessions);
    	addOption("topic",false,"show all topics.",this::showTopics);
    	addOption("net",false,"show network stats.",this::showNetworkStats);
    	addOption("db",false,"show dashboard.",this::showDashboard);	
    	//
    	rpcServer=Jazmin.getServer(RpcServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(rpcServer==null){
			 out.println("can not find RPCServer.");
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	out.println(rpcServer.info());
	}
    //
    //
    private void showServices(String args){
		String format="%-5s: %-100s\n";
		int i=1;
		List<String> services=rpcServer.getServiceNames();
		Collections.sort(services);
		out.println("total "+services.size()+" services");
		out.format(format,"#","NAME");	
		for(String s:services){
			out.format(format,i++,s);
		};
    }
    //
    //
    private void showDashboard(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(200,80);
    	lastInvokeCount=Jazmin.dispatcher.getTotalInvokeCount();
    	lastSubmitCount=Jazmin.dispatcher.getTotalSubmitCount();
    	maxInvokeTps=0;
    	maxSubmitTps=0;
    	
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		printLine('=', 120);
    		showSessions(5);
    		printLine('=', 120);
    		showPushCount(3);
    		printLine('=', 120);
    		showNetworkStats0();
    		printLine('=', 120);
    		showIOWorkerInfo();
    		printLine('=', 120);
        	showThreadPoolTps0(chart,tw);
        	out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
      	stdin.read();
    }
    //
    private void showPushCount(int maxCount){
    	Map<String,LongAdder>topicCount=rpcServer.getPushMessageCountMap();
    	String format="%-10s %-20s %-10s\n";
    	out.printf(format,
    			"#",
    			"TOPIC",
    			"COUNT");
    	int i=1;
    	for(Entry<String,LongAdder>e:topicCount.entrySet()){
    		out.printf(format, i++,e.getKey(),e.getValue());
    		if(i>maxCount){
    			break;
    		}
    	}
    }
    //
    private void showIOWorkerInfo(){
    	String format="%-10s %-15s %-10s %-15s %-15s %-15s\n";
    	out.printf(format,
    			"POOLSIZE",
    			"REQUEUESIZE",
    			"TASKCOUNT",
    			"ACTIVECOUNT",
    			"COMPLETEDTASK",
    			"TOTALEXECUTE");
    	IOWorker worker=rpcServer.getIOWorker();
    	out.printf(format,
    			worker.getPoolSize(),
    			worker.getRequestQueueSize(),
    			worker.getTaskCount(),
    			worker.getActiveCount(),
    			worker.getCompletedTaskCount(),
    			worker.getTotalExecuteCount());
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
    	printLine('=', 120);
		out.println("thread pool invoke tps chart. current:"+invokeTps+"/s");
		tw.fmagenta();
		chart.reset();
		out.println(chart.draw());
		tw.reset();
		
    }
  
    //
    private void showSessions(String args){
    	showSessions(Integer.MAX_VALUE);
    }
    //
    private void showSessions(int maxCount){
		String format="%-5s:%-30s %-15s %-10s %-10s %-10s %-10s %-10s %-15s %-10s\n";
		int i=1;
		List<RpcSession> sessions=rpcServer.getSessions();
		out.println("total "+sessions.size()+" sessions");
		out.format(format,"#",
				"PRINCIPAL",
				"HOSTADDRESS",
				"PORT",
				"DISPUSH",
				"SEND",
				"RECEIVE",
				"PUSH",
				"NETWORK TIME",
				"CREATETIME");	
		for(RpcSession s:sessions){
			long t=s.getTotalNetworkTime();
			if(s.getReceivedPackageCount()!=0){
				t=t/s.getReceivedPackageCount();
			}
			String network=s.getMinNetworkTime()+"/"+s.getMaxNetworkTime()+"/"+t;
			out.format(format,
					i++,
					cut(s.getPrincipal(),30),
					s.getRemoteHostAddress(),
					s.getRemotePort(),
					s.isDisablePushMessage(),
					s.getSentPackageCount(),
					s.getReceivedPackageCount(),
					s.getPushedPackageCount(),
					network,
					formatDate(s.getCreateTime()));
		};
    }
    //
    //
    private void showTopics(String args){
		int i=1;
		String format="%-5s: %-20s %-10s\n";
		List<String> topicNames=rpcServer.getTopicNames();
		Map<String,LongAdder>topicCount=rpcServer.getPushMessageCountMap();
		out.println("total "+topicNames.size()+" topics");
		LongAdder zero=new LongAdder();
		for(String topic:topicNames){
			out.format(format,i++,topic,topicCount.getOrDefault(topic, zero));
			for(RpcSession s:rpcServer.getTopicSession(topic)){
				out.format(format,"","",s);
			}
		}
    }
    //
    private void showNetworkStats(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	lastInBoundBytes=rpcServer.getInBoundBytes();
    	lastOutBoundBytes=rpcServer.getOutBoundBytes();
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showNetworkStats0();
    		out.flush();
    		Thread.sleep(1000);
    	}
    }
    //
    private long lastInBoundBytes=0;
    private long lastOutBoundBytes=0;
    //
    private void showNetworkStats0(){
    	long inBoundBytes=rpcServer.getInBoundBytes();
    	long outBoundBytes=rpcServer.getOutBoundBytes();
    	String format="%-10s %-30s %-20s %-20s\n";
    	out.printf(format,
    			"TYPE",
    			"DATE",
    			"IN",
    			"OUT");
    	long ii=inBoundBytes-lastInBoundBytes;
    	long oo=outBoundBytes-lastOutBoundBytes;
    	
    	out.printf(format,
    			"RATE/S",
    			formatDate(new Date()),
    			DumpUtil.byteCountToString(ii),
    			DumpUtil.byteCountToString(oo));
    	lastInBoundBytes=inBoundBytes;
    	lastOutBoundBytes=outBoundBytes;
    	//
    	ii=inBoundBytes;
    	oo=outBoundBytes;
    	out.printf(format,
    			"TOTAL",
    			formatDate(new Date()),
    			DumpUtil.byteCountToString(ii),
    			DumpUtil.byteCountToString(oo));
    }
    
}
