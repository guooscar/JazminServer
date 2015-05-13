package jazmin.server.sip;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.util.DumpUtil;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class SipServerCommand extends ConsoleCommand {
	private SipServer server;

	public SipServerCommand() {
		super();
		id = "sipsrv";
		desc = "sip server ctrl command";
		addOption("i", false, "show server information.", this::showServerInfo);
		addOption("s", false, "show server sessions.", this::showSessions);
		addOption("c", false, "show server channels.", this::showChannels);
		addOption("l", false, "show location store.", this::showLocationStore);
		addOption("so", true, "show  session object.", this::showSessionObject);
		//
		server = Jazmin.getServer(SipServer.class);
	}
	//
	@Override
	public void run() throws Exception {
		if (server == null) {
			err.println("can not find SipServer.");

			return;
		}
		super.run();
	}
	//
	private void showServerInfo(String args) {
		String format = "%-25s: %-10s\n";
		out.printf(format, "hostAddress", server.getHostAddress());
		out.printf(format, "port", server.getPort());
		out.printf(format, "tlsPort", server.getTlsPort());
		out.printf(format, "webSocketPort",server.getWebSocketPort());
		out.printf(format, "publicAddress", server.getPublicAddress());
		out.printf(format, "publicPort", server.getPublicPort());
		out.printf(format, "sessionTimeout", server.getSessionTimeout());
		out.printf(format, "messageHandler", server.getMessageHandler());
	}
	//
	private void showLocationStore(String args){
    	List<SipLocationBinding>bindings=server.getLocationBindings();
    	out.format("total %d bindings\n",bindings.size());
    	String format="%-5s %-25s %-25s %-10s %-15s %-50s %-40s\n";
    	out.printf(format,
				"#",
				"AOR",
				"CONTACT",
				"EXPIRES",
    			"CREATETIME",
    			"CONNECTION",
    			"CALLID");
    	int idx=1;
    	for(SipLocationBinding b:bindings){
    		out.printf(format,
        			idx++,
        			cut(b.getAor()+"",25),
        			cut(b.getContact()+"",25),
        			b.getExpires(),
        			formatDate(b.getCreateTime()),
        			b.getConnection(),
        			b.getCallId());
    	}
    }
	//
	private void showSessions(String args){
		TablePrinter tp=TablePrinter.create(out)
				.length(10,20,5,15,15,30)
				.headers("SESSIONID",
    			"REMOTEADDRESS",
    			"REMOTE PORT",
    			"CREATETIME",
    			"LASTACCTIME",
    			"CALLID");
    	List<SipSession>sessions=server.getSessions();
    	for(SipSession s:sessions){
    		tp.print(s.getSessionId(),
        			s.getRemoteAddress(),
        			s.getRemotePort(),
        			formatDate(s.getCreateTime()),
        			formatDate(s.getLastAccessTime()),
        			s.getCallId());
    	}
    }
	//
	private void showChannels(String args){
		TablePrinter tp=TablePrinter.create(out)
				.length(15,10,15,10,15,10,10,10,15)
				.headers("ID",
						"TRANSPORT",
						"REMOTEADDRESS",
		    			"REMOTEPORT",
		    			"LOCALADDRESS",
		    			"LOCALPORT",
		    			"SENTCNT",
		    			"RECECNT",
		    			"CREATETIME");
    	List<SipChannel>channels=server.getChannels();
    	for(SipChannel s:channels){
    		tp.print(
        			s.id,
        			s.transport,
        			s.remoteAddress,
        			s.remotePort,
        			s.localAddress,
        			s.localPort,
        			s.messageSentCount,
        			s.messageReceivedCount,
        			formatDate(s.createTime));
    	}
    }
	//
	private void showSessionObject(String args){
		SipSession s=server.getSession(args);
		if(s==null){
			out.println("<null>");
		}else{
			out.println(DumpUtil.dump(s.userObject));
		}
	}
}
