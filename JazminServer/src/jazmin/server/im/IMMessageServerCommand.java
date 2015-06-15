package jazmin.server.im;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.FormPrinter;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.ascii.TerminalWriter;
import jazmin.server.console.builtin.ConsoleCommand;
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
    	out.println(messageServer.info());
	}
    //
    //
    private void showServices(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(30,15,15,15)
    			.headers("NAME","SYNCONSESSION","CONTINUATION","RESTRICTRATE");
		List<IMServiceStub> services=messageServer.getServices();
		Collections.sort(services);
		for(IMServiceStub s:services){
			tp.print(
					s.serviceId,
					s.isSyncOnSessionService,
					s.isContinuationService,
					s.isRestrictRequestRate);
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
    	FormPrinter fp=FormPrinter.create(out,20);
    	fp.print("id",session.getId());
    	fp.print("principal",session.getPrincipal());
    	fp.print("userAgent",session.getUserAgent());
    	fp.print("remoteHostAddress",session.getRemoteHostAddress());
    	fp.print("remotePort",session.getRemotePort());
    	fp.print("lastAccessTime",formatDate(new Date(session.getLastAccessTime())));
    	fp.print("createTime",formatDate(session.getCreateTime()));
    	fp.print("receiveMessageCount",session.getReceiveMessageCount());
    	fp.print("sentMessageCount",session.getSentMessageCount());
    	fp.print("channels",session.getChannels());
    	fp.print("userObject",DumpUtil.dump(session.getUserObject()));		
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
    	TablePrinter tp=TablePrinter.create(out)
    			.length(10,20,10,15,10,10,10,15,15,10)
    			.headers("ID",
    					"PRINCIPAL",
    					"USERAGENT",
    					"HOST",
    					"PORT",
    					"RECEIVE",
    					"SENT",
    					"LACC",
    					"CRTIME",
    					"SYNCFLAG");
    	
		List<IMSession> sessions=messageServer.getSessions();
		//
		String querySql=cli.getOptionValue('q');
		if(querySql!=null){
			sessions=BeanUtil.query(sessions,querySql);
		}
		
		for(IMSession s:sessions){
			tp.print(
					s.getId(),
					s.getPrincipal(),
					s.getUserAgent(),
					s.getRemoteHostAddress(),
					s.getRemotePort(),
					s.getReceiveMessageCount(),
					s.getSentMessageCount(),
					formatDate(new Date(s.getLastAccessTime())),
					formatDate(s.getCreateTime()),
					s.isProcessSyncService());
		};
    }
    //
    private void showChannels(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(10,20,10)
    			.headers("ID","AUTOREMOVESESSION","CREATETIME");
		List<IMChannel> channels=messageServer.getChannels();
		for(IMChannel s:channels){
			tp.print(
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
    	FormPrinter fp=FormPrinter.create(out,20);
    	fp.print("id",channel.getId());
    	fp.print("autoRemoveDisconnectedSession",channel.isAutoRemoveDisconnectedSession());
    	fp.print("createTime",formatDate(new Date(channel.getCreateTime())));
    	fp.print("userObject",DumpUtil.dump(channel.getUserObject()));		
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
    	String format="%-10s %-30s %-10s %-10s\n";
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
