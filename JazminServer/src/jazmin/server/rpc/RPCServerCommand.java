package jazmin.server.rpc;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.AsciiChart;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RPCServerCommand extends ConsoleCommand {
    private RPCServer rpcServer;
	public RPCServerCommand() {
    	super();
    	id="rpcsrv";
    	desc="rpc server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("srv",false,"show services.",this::showServices);
    	addOption("session",false,"show all sessions.",this::showSessions);
    	addOption("sessiontop",false,"show all sessions.",this::showSessionTop);
    	addOption("topic",false,"show all topics.",this::showTopics);
    	addOption("network",false,"show network stats.",this::showNetworkStats);
    	addOption("dashboard",false,"show dashboard.",this::showDashboard);	
    	//
    	rpcServer=Jazmin.getServer(RPCServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(rpcServer==null){
			 err.println("can not find RPCServer.");
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	String format="%-20s: %-10s\n";
		out.printf(format,"port",rpcServer.getPort());
		out.printf(format,"idleTime",rpcServer.getIdleTime());
		int idx=1;
		for(String host:rpcServer.getAcceptRemoteHosts()){
			out.printf(format,"acceptHost-"+idx++,host);		
		}
	}
    //
    //
    private void showServices(String args){
		String format="%-5s: %-100s\n";
		int i=0;
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
    		lastInBoundBytes=rpcServer.getInBoundBytes();
        	lastOutBoundBytes=rpcServer.getOutBoundBytes();
    		out.println("press any key to quit.");
    		printLine('=', 120);
    		showSessions(5);
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
    private void showIOWorkerInfo(){
    	String format="%-10s %-15s %-10s %-15s %-15s %-15s\n";
    	out.printf(format,
    			"POOLSIZE",
    			"REQUESTQUEUESIZE",
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
    private void showSessionTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showSessions(args);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    }
    //
    private void showSessions(String args){
    	showSessions(Integer.MAX_VALUE);
    }
    //
    private void showSessions(int maxCount){
		String format="%-5s: %-20s %-15s %-10s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<RPCSession> sessions=rpcServer.getSessions();
		out.println("total "+sessions.size()+" sessions");
		out.format(format,"#",
				"PRINCIPAL",
				"HOSTADDRESS",
				"PORT",
				"DISPUSH",
				"SEND",
				"RECEIVE",
				"CREATETIME");	
		for(RPCSession s:sessions){
			out.format(format,
					i++,
					s.getPrincipal(),
					s.getRemoteHostAddress(),
					s.getRemotePort(),
					s.isDisablePushMessage(),
					s.getSentPackageCount(),
					s.getReceivedPackageCount(),
					formatDate(s.getCreateTime()));
		};
    }
    //
    //
    private void showTopics(String args){
		int i=0;
		String format="%-5s: %-20s\n";
		List<String> topicNames=rpcServer.getTopicNames();
		out.println("total "+topicNames.size()+" topics");
		for(String topic:topicNames){
			out.format(format,i++,topic);
			for(RPCSession s:rpcServer.getTopicSession(topic)){
				out.format(format,"",s);
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
    	String format="%-30s %-10s %-10s %-10s %-10s %-10s %-10s\n";
    	out.printf(format,
    			"DATE",
    			"IN BYTE",
    			"OUT BYTE",
    			"IN KB",
    			"OUT KB",
    			"IN MB",
    			"OUT MB");
    	long ii=inBoundBytes-lastInBoundBytes;
    	long oo=outBoundBytes-lastOutBoundBytes;
    	
    	out.printf(format,
    			formatDate(new Date()),
    			ii,
    			oo,
    			ii/1024,
    			oo/1024,
    			ii/(1024*1024),
    			oo/(1024*1024));
    	lastInBoundBytes=inBoundBytes;
    	lastOutBoundBytes=outBoundBytes;
    }
    
}
