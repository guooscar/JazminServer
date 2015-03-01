package jazmin.server.msg;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.AsciiChart;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
import jazmin.util.BeanUtil;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class MessageServerCommand extends ConsoleCommand {
    private MessageServer messageServer;
	public MessageServerCommand() {
    	super();
    	id="msgsrv";
    	desc="message server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("srv",false,"show services.",this::showServices);
    	addOption("session",false,"show all sessions.",this::showSessions);
    	addOption("q",true,"session query sql.",null);
    	addOption("sp",false,"show session plot.",this::showSessionPlot);
    	addOption("channel",false,"show all channels.",this::showChannels);
    	addOption("so",true,"show session info",this::showSessionInfo); 
    	addOption("kick",true,"kick session",this::kickSession);
    	addOption("co",true,"show channel info",this::showChannelInfo); 
    	addOption("network",false,"show network stats.",this::showNetworkStats);
    	//
    	messageServer=Jazmin.server(MessageServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(messageServer==null){
			 err.println("can not find MessageServer.");
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	String format="%-20s: %-10s\n";
		out.printf(format,"port",messageServer.port());
		out.printf(format,"idleTime",messageServer.idleTime());
		out.printf(format,"maxChannelCount",messageServer.maxChannelCount());
		out.printf(format,"maxSessionCount",messageServer.maxSessionCount());
		out.printf(format,"messageType",messageServer.messageType());
		out.printf(format,"serviceFilter",messageServer.serviceFilter());
		out.printf(format,"sessionLifecycleListener",messageServer.sessionLifecycleListener());
		//
		out.printf(format,"sessionCount",messageServer.sessionCount());
		out.printf(format,"channelCount",messageServer.channelCount());
		
	}
    //
    //
    private void showServices(String args){
		String format="%-5s : %-30s %-10s %-10s %-10s\n";
		int i=0;
		List<ServiceStub> services=messageServer.services();
		Collections.sort(services);
		out.println("total "+services.size()+" services");
		out.format(format,"#","NAME","ASYNC","CONTINUATION","DISABLERSP");	
		for(ServiceStub s:services){
			out.format(format,
					i++,
					s.serviceId,
					s.isAsyncService,
					s.isContinuationService,
					s.isDisableResponseService);
		};
    }
    //
    //
    private void showSessionInfo(String args){
    	Session session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		err.println("can not find session:"+args);
    		return;
    	}
    	String format="%-20s: %-10s\n";
		out.printf(format,"id",session.id());
		out.printf(format,"principal",session.principal());
		out.printf(format,"userAgent",session.userAgent());
		out.printf(format,"isActive",session.isActive());
		out.printf(format,"remoteHostAddress",session.remoteHostAddress());
		out.printf(format,"remotePort",session.remotePort());
		out.printf(format,"lastAccessTime",formatDate(new Date(session.lastAccessTime())));
		out.printf(format,"createTime",formatDate(session.createTime()));
		out.printf(format,"totalMessageCount",session.totalMessageCount());
		out.printf(format,"channels",session.channels());
		out.printf(format,"userObject",DumpUtil.dump(session.userObject()));		
	}
    //
    //
    private void kickSession(String args){
    	Session session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		err.println("can not find session:"+args);
    		return;
    	}
    	session.kick("kick by jasmin console.");	
	}
    //
    private void showSessions(String args){
		String format="%-5s: %-5s %-10s %-10s %-10s %-15s %-10s %-10s %-15s %-15s %-10s\n";
		int i=0;
		List<Session> sessions=messageServer.sessions();
		//
		String querySql=cli.getOptionValue('q');
		if(querySql!=null){
			sessions=BeanUtil.query(sessions,querySql);
		}
		//
		out.println("total "+sessions.size()+" sessions");
		out.format(format,"#",
				"ID",
				"PRINCIPAL",
				"USERAGENT",
				"ACTIVE",
				"HOST",
				"PORT",
				"TOTALMSG",
				"LACC",
				"CRTIME",
				"SYNCFLAG");	
		for(Session s:sessions){
			out.format(format,
					i++,
					s.id(),
					s.principal(),
					s.userAgent(),
					s.isActive(),
					s.remoteHostAddress(),
					s.remotePort(),
					s.totalMessageCount(),
					formatDate(new Date(s.lastAccessTime())),
					formatDate(s.createTime()),
					s.processSyncService());
		};
    }
    //
    private void showChannels(String args){
		String format="%-5s: %-10s %-10s \n";
		int i=0;
		List<Channel> channels=messageServer.channels();
		out.println("total "+channels.size()+" channels");
		out.format(format,"#","ID","CREATETIME");	
		for(Channel s:channels){
			out.format(format,
					i++,
					s.id(),
					formatDate(new Date(s.createTime())));
		};
    }
    private void showChannelInfo(String args){
    	Channel channel=messageServer.channel(args);
    	if(channel==null){
    		err.println("can not find channel:"+args);
    		return;
    	}
    	String format="%-20s: %-10s\n";
		out.printf(format,"id",channel.id());
		out.printf(format,"createTime",formatDate(new Date(channel.createTime())));
		out.printf(format,"userObject",DumpUtil.dump(channel.userObject()));		
	}
    //
    //
    private void showSessionPlot(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(160,80);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		out.println("current session count:"+messageServer.sessionCount());
    		chart.addValue(messageServer.sessionCount());
    		chart.reset();
    		tw.fmagenta();
    		out.println(chart.draw());
    		tw.reset();
    		out.flush();
    		Thread.sleep(1000);
    	}
    	stdin.read();
    }
    //
    private void showNetworkStats(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	lastInBoundBytes=messageServer.inBoundBytes();
    	lastOutBoundBytes=messageServer.outBoundBytes();
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showNetworkStats0();
    		out.flush();
    		Thread.sleep(1000);
    	}
    	stdin.read();
    }
    //
    private long lastInBoundBytes=0;
    private long lastOutBoundBytes=0;
    //
    private void showNetworkStats0(){
    	long inBoundBytes=messageServer.inBoundBytes();
    	long outBoundBytes=messageServer.outBoundBytes();
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
