package jazmin.server.im;
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
public class IMMessageServerCommand extends ConsoleCommand {
    private IMMessageServer messageServer;
	public IMMessageServerCommand() {
    	super();
    	id="imsgsrv";
    	desc="message server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("srv",false,"show services.",this::showServices);
    	addOption("s",false,"show all sessions.",this::showSessions);
    	addOption("q",true,"session query sql.",null);
    	addOption("sp",false,"show session plot.",this::showSessionPlot);
    	addOption("channel",false,"show all channels.",this::showChannels);
    	addOption("so",true,"show session info",this::showSessionInfo); 
    	addOption("kick",true,"kick session",this::kickSession);
    	addOption("co",true,"show channel info",this::showChannelInfo); 
    	addOption("net",false,"show network stats.",this::showNetworkStats);
    	//
    	messageServer=Jazmin.getServer(IMMessageServer.class);
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
		out.printf(format,"port",messageServer.getPort());
		out.printf(format,"idleTime",messageServer.getIdleTime());
		out.printf(format,"maxChannelCount",messageServer.getMaxChannelCount());
		out.printf(format,"maxSessionCount",messageServer.getMaxSessionCount());
		out.printf(format,"serviceFilter",messageServer.getServiceFilter());
		out.printf(format,"sessionLifecycleListener",messageServer.getSessionLifecycleListener());
		//
		out.printf(format,"sessionCount",messageServer.getSessionCount());
		out.printf(format,"channelCount",messageServer.getChannelCount());
		
	}
    //
    //
    private void showServices(String args){
		String format="%-5s : %-30s %-10s %-10s\n";
		int i=0;
		List<IMServiceStub> services=messageServer.getServices();
		Collections.sort(services);
		out.println("total "+services.size()+" services");
		out.format(format,"#","NAME","ASYNC","CONTINUATION");	
		for(IMServiceStub s:services){
			out.format(format,
					i++,
					"0x"+Integer.toHexString(s.serviceId),
					s.isAsyncService,
					s.isContinuationService);
		};
    }
    //
    //
    private void showSessionInfo(String args){
    	IMSession session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		err.println("can not find session:"+args);
    		return;
    	}
    	String format="%-20s: %-10s\n";
		out.printf(format,"id",session.getId());
		out.printf(format,"principal",session.getPrincipal());
		out.printf(format,"userAgent",session.getUserAgent());
		out.printf(format,"remoteHostAddress",session.getRemoteHostAddress());
		out.printf(format,"remotePort",session.getRemotePort());
		out.printf(format,"lastAccessTime",formatDate(new Date(session.getLastAccessTime())));
		out.printf(format,"createTime",formatDate(session.getCreateTime()));
		out.printf(format,"totalMessageCount",session.getTotalMessageCount());
		out.printf(format,"channels",session.getChannels());
		out.printf(format,"userObject",DumpUtil.dump(session.getUserObject()));		
	}
    //
    //
    private void kickSession(String args){
    	IMSession session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		err.println("can not find session:"+args);
    		return;
    	}
    	session.kick("kick by jasmin console.");	
	}
    //
    private void showSessions(String args){
		String format="%-5s: %-5s %-10s %-10s  %-15s %-10s %-10s %-15s %-15s %-10s\n";
		int i=0;
		List<IMSession> sessions=messageServer.getSessions();
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
				"HOST",
				"PORT",
				"TOTALMSG",
				"LACC",
				"CRTIME",
				"SYNCFLAG");	
		for(IMSession s:sessions){
			out.format(format,
					i++,
					s.getId(),
					s.getPrincipal(),
					s.getUserAgent(),
					s.getRemoteHostAddress(),
					s.getRemotePort(),
					s.getTotalMessageCount(),
					formatDate(new Date(s.getLastAccessTime())),
					formatDate(s.getCreateTime()),
					s.isProcessSyncService());
		};
    }
    //
    private void showChannels(String args){
		String format="%-5s: %-10s %-20s %-10s \n";
		int i=0;
		List<IMChannel> channels=messageServer.getChannels();
		out.println("total "+channels.size()+" channels");
		out.format(format,"#","ID","AUTOREMOVESESSION","CREATETIME");	
		for(IMChannel s:channels){
			out.format(format,
					i++,
					s.getId(),
					s.isAutoRemoveDisconnectedSession(),
					formatDate(new Date(s.getCreateTime())));
		};
    }
    private void showChannelInfo(String args){
    	IMChannel channel=messageServer.getChannel(args);
    	if(channel==null){
    		err.println("can not find channel:"+args);
    		return;
    	}
    	String format="%-20s: %-10s\n";
		out.printf(format,"id",channel.getId());
		out.printf(format,"autoRemoveDisconnectedSession",channel.isAutoRemoveDisconnectedSession());
		out.printf(format,"createTime",formatDate(new Date(channel.getCreateTime())));
		out.printf(format,"userObject",DumpUtil.dump(channel.getUserObject()));		
	}
    //
    //
    private void showSessionPlot(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	AsciiChart chart=new AsciiChart(160,80);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		out.println("current session count:"+messageServer.getSessionCount());
    		chart.addValue(messageServer.getSessionCount());
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
    	lastInBoundBytes=messageServer.getInBoundBytes();
    	lastOutBoundBytes=messageServer.getOutBoundBytes();
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
    	long inBoundBytes=messageServer.getInBoundBytes();
    	long outBoundBytes=messageServer.getOutBoundBytes();
    	String format="%-10s %-30s %-10s %-10s %-10s %-10s %-10s %-10s\n";
    	out.printf(format,
    			"TYPE",
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
    			"RATE/S",
    			formatDate(new Date()),
    			ii,
    			oo,
    			ii/1024,
    			oo/1024,
    			ii/(1024*1024),
    			oo/(1024*1024));
    	lastInBoundBytes=inBoundBytes;
    	lastOutBoundBytes=outBoundBytes;
    	//
    	ii=inBoundBytes;
    	oo=outBoundBytes;
    	out.printf(format,
    			"TOTAL",
    			formatDate(new Date()),
    			ii,
    			oo,
    			ii/1024,
    			oo/1024,
    			ii/(1024*1024),
    			oo/(1024*1024));
    }
}
