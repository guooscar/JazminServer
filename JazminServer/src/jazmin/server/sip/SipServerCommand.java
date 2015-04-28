package jazmin.server.sip;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
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
		addOption("l", false, "show location store.", this::showLocationStore);
		addOption("sa", true, "show  session object.", this::showSessionObject);
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
		out.printf(format, "publicAddress", server.getPublicAddress());
		out.printf(format, "publicPort", server.getPublicPort());
		out.printf(format, "sessionTimeout", server.getSessionTimeout());
		out.printf(format, "messageHandler", server.getMessageHandler());
	}
	//
	private void showLocationStore(String args){
    	List<SipLocationBinding>bindings=server.getLocationBindings();
    	out.format("total %d bindings\n",bindings.size());
    	String format="%-5s %-25s %-25s %-10s %-15s %-40s %-40s\n";
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
        			b.getAor(),
        			b.getContact(),
        			b.getExpires(),
        			formatDate(b.getCreateTime()),
        			b.getConnection(),
        			b.getCallId());
    	}
    }
	//
	private void showSessions(String args){
    	List<SipSession>sessions=server.getSessions();
    	out.format("total %d sessions\n",sessions.size());
    	String format="%-5s %-10s %-20s %-5s %-15s %-15s  %-30s\n";
    	out.printf(format,
				"#",
				"SESSIONID",
    			"REMOTEADDRESS",
    			"REMOTE PORT",
    			"CREATETIME",
    			"LASTACCTIME",
    			"CALLID");
    	int idx=1;
    	for(SipSession s:sessions){
    		out.printf(format,
        			idx++,
        			s.getSessionId(),
        			s.getRemoteAddress(),
        			s.getRemotePort(),
        			formatDate(s.getCreateTime()),
        			formatDate(s.getLastAccessTime()),
        			s.getCallId());
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
