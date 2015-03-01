package jazmin.driver.rpc;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.misc.InvokeStat;
import jazmin.server.console.AsciiChart;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
import jazmin.server.rpc.RPCSession;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class JazminRPCDriverCommand extends ConsoleCommand {
    private JazminRPCDriver driver;
	public JazminRPCDriverCommand() {
    	super();
    	id="rpcdriver";
    	desc="jazmin rpc driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("session",false,"show all sessions.",this::showSessions);
    	addOption("sessiontop",false,"show all sessions.",this::showSessionTop);
    	addOption("stat",false,"show method stat.",this::showMethodStats);
    	addOption("tps",false,"show invoke tps.",this::showInvokeTps);
    	//
    	driver=Jazmin.driver(JazminRPCDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(driver==null){
			 err.println("can not find JazminRPCDriver.");
			 return;
		 }
		 super.run();
	}
    //
    private void showDriverInfo(String args)throws Exception{
    	String format="%-20s: %-10s\n";
		out.printf(format,"pushCallback",driver.pushCallback());
		out.printf(format,"principal",driver.principal());	
		//
		out.println("remote servers");
		driver.remoteServers().forEach(server->{
			out.format(format,
					server.cluster+"."+server.name,
					server.remoteHostAddress+":"+server.remotePort);	
		});
		out.println("async proxy");
		driver.asyncProxys().forEach(out::println);
		out.println("sync proxy");
		driver.syncProxys().forEach(out::println);	
    }
    //
    private void showSessionTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showSessions(args);
    		out.flush();
    		Thread.sleep(1000);
    	}
    }
    //
    private void showSessions(String args){
		String format="%-5s : %-20s %-20s %-10s %-15s %-10s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<RPCSession> sessions=driver.sessions();
		out.println("total "+sessions.size()+" sessions");
		out.format(format,"#",
				"PRINCIPAL",
				"CLUSTER",
				"CONNECTED",
				"HOSTADDRESS",
				"PORT",
				"DISPUSH",
				"SEND",
				"RECEIVE",
				"CREATETIME");	
		for(RPCSession s:sessions){
			out.format(format,
					i++,
					s.principal(),
					s.cluster(),
					s.isConnected(),
					s.remoteHostAddress(),
					s.remotePort(),
					s.disablePushMessage(),
					s.sendPackageCount(),
					s.receivePackageCount(),
					formatDate(s.createTime()));
		};
    }
    //
    private void showMethodStats(String args){
    	String format="%-5s : %-50s %-10s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<InvokeStat>stats=driver.invokeStats();
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
    //
    private long lastInvokeCount=0;
    private long maxInvokeTps=0;
    //
    private void showInvokeTps(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(160,80);
    	lastInvokeCount=driver.totalInvokeCount();
    	maxInvokeTps=0;
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showInvokeTps(chart,tw);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    	stdin.read();
    }
    //
    private void showInvokeTps(AsciiChart chart,TerminalWriter tw){
    	long invokeCount=driver.totalInvokeCount();
    	long invokeTps=invokeCount-lastInvokeCount;
    	if(invokeTps>maxInvokeTps){
    		maxInvokeTps=invokeTps;
    	}
    	chart.addValue((int)(invokeTps));
    	lastInvokeCount=invokeCount;
    	//
    	out.println("-----------------------------------------------------");
		out.println("rpc driver invoke tps chart. total:"+invokeCount+
					" max:"+maxInvokeTps+
					" current:"+invokeTps+"/s");
		tw.fmagenta();
		chart.reset();
		out.println(chart.draw());
		tw.reset();
		
    }
}
