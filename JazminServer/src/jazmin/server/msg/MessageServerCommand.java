package jazmin.server.msg;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.ascii.TerminalWriter;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.server.msg.codec.DefaultCodecFactory;
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
    	super(true);
    	id="msgsrv";
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
    	messageServer=Jazmin.getServer(MessageServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(messageServer==null){
			 out.println("can not find MessageServer.");
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
    			.length(30,15,15,15,15)
    			.headers("NAME","SYNCONSESSION","CONTINUATION","DISABLERSP","RESTRICTRATE");
		List<ServiceStub> services=messageServer.getServices();
		Collections.sort(services);
		for(ServiceStub s:services){
			tp.print(
					s.serviceId,
					s.isSyncOnSessionService,
					s.isContinuationService,
					s.isDisableResponseService,
					s.isRestrictRequestRate);
		}
    }
    //
    //
    private void showSessionInfo(String args){
    	Session session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		out.println("can not find session:"+args);
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
		out.printf(format,"sentMessageCount",session.getSentMessageCount());
		out.printf(format,"receiveMessageCount",session.getReceiveMessageCount());
		
		out.printf(format,"channels",session.getChannels());
		out.printf(format,"userObject",DumpUtil.dump(session.getUserObject()));		
	}
    //
    //
    private void kickSession(String args){
    	Session session=messageServer.getSessionByPrincipal(args);
    	if(session==null){
    		out.println("can not find session:"+args);
    		return;
    	}
    	session.kick("kick by jasmin console.");	
	}
    //
    private void showSessions(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(6,48,6,10,15,10,10,10,10,15,15,10)
    			.headers("ID",
    					"PRINCIPAL",
    					"MSGTYP",
    					"USERAGENT",
    					"HOST",
    					"PORT",
    					"CONN",
    					"RECEIVE",
    					"SENT",
    					"LACC",
    					"CRTIME",
    					"SYNCFLAG");
    	
		List<Session> sessions=messageServer.getSessions();
		//
		String querySql=cli.getOptionValue('q');
		if(querySql!=null){
			sessions=BeanUtil.query(sessions,querySql);
		}
		
		for(Session s:sessions){
			String msgType="raw-"+s.getMessageType();
			if(s.getMessageType()==DefaultCodecFactory.FORMAT_JSON){
				msgType="json";
			}else if(s.getMessageType()==DefaultCodecFactory.FORMAT_ZJSON){
				msgType="zjson";
			}else if(s.getMessageType()==DefaultCodecFactory.FORMAT_AMF){
				msgType="amf";
			}
			tp.print(
					s.getId(),
					s.getPrincipal(),
					msgType,
					s.getUserAgent(),
					s.getRemoteHostAddress(),
					s.getRemotePort(),
					s.getConnectionType(),
					s.getReceiveMessageCount(),
					s.getSentMessageCount(),
					formatDate(new Date(s.getLastAccessTime())),
					formatDate(s.getCreateTime()),
					s.isProcessSyncService());
		};
    }
    //
    //
    private void showChannels(String args){
		String format="%-5s: %-30s %-20s %-10s \n";
		int i=1;
		List<Channel> channels=messageServer.getChannels();
		out.println("total "+channels.size()+" channels");
		out.format(format,"#","ID","AUTOREMOVESESSION","SESSIONCOUNT","CREATETIME");	
		for(Channel s:channels){
			out.format(format,
					i++,
					cut(s.getId(),64),
					s.isAutoRemoveDisconnectedSession(),
					s.getSessions().size(),
					formatDate(new Date(s.getCreateTime())));
		};
    }
    private void showChannelInfo(String args){
    	Channel channel=messageServer.getChannel(args);
    	if(channel==null){
    		out.println("can not find channel:"+args);
    		return;
    	}
    	String format="%-20s: %-10s\n";
		out.printf(format,"id",channel.getId());
		out.printf(format,"autoRemoveDisconnectedSession",channel.isAutoRemoveDisconnectedSession());
		out.printf(format,"createTime",formatDate(new Date(channel.getCreateTime())));
		out.printf(format,"userObject",DumpUtil.dump(channel.getUserObject()));	
		for(Session session:channel.getSessions()){
			out.printf(format,"session",session.getId()+"#"+session.getPrincipal());
		}
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
