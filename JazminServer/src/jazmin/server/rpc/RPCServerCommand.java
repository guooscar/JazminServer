package jazmin.server.rpc;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
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
        	
    	//
    	rpcServer=Jazmin.server(RPCServer.class);
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
		out.printf(format,"port",rpcServer.port());
		out.printf(format,"idleTime",rpcServer.idleTime());
		int idx=1;
		for(String host:rpcServer.acceptRemoteHosts()){
			out.printf(format,"acceptHost-"+idx++,host);		
		}
	}
    //
    //
    private void showServices(String args){
		String format="%-5s: %-100s\n";
		int i=0;
		List<String> services=rpcServer.serviceNames();
		Collections.sort(services);
		out.println("total "+services.size()+" services");
		out.format(format,"#","NAME");	
		for(String s:services){
			out.format(format,i++,s);
		};
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
		String format="%-5s: %-20s %-15s %-10s %-10s %-10s %-10s %-10s\n";
		int i=0;
		List<RPCSession> sessions=rpcServer.sessions();
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
					s.principal(),
					s.remoteHostAddress(),
					s.remotePort(),
					s.disablePushMessage(),
					s.sendPackageCount(),
					s.receivePackageCount(),
					formatDate(s.createTime()));
		};
    }
    //
    //
    private void showTopics(String args){
		int i=0;
		String format="%-5s: %-20s\n";
		List<String> topicNames=rpcServer.topicNames();
		out.println("total "+topicNames.size()+" topics");
		for(String topic:topicNames){
			out.format(format,i++,topic);
			for(RPCSession s:rpcServer.topicSession(topic)){
				out.format(format,"",s);
			}
		}
    }
    //
    private void showNetworkStats(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	lastInBoundBytes=rpcServer.inBoundBytes();
    	lastOutBoundBytes=rpcServer.outBoundBytes();
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
    	long inBoundBytes=rpcServer.inBoundBytes();
    	long outBoundBytes=rpcServer.outBoundBytes();
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
