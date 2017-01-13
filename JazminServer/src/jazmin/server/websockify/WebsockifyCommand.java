package jazmin.server.websockify;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class WebsockifyCommand extends ConsoleCommand {
	private WebsockifyServer server;

	public WebsockifyCommand() {
		super(true);
		id = "websockify";
		desc = "websockify server ctrl command";
		addOption("i", false, "show server information.", this::showServerInfo);
		addOption("c", false, "show server channels.", this::showChannels);
		//
		server = Jazmin.getServer(WebsockifyServer.class);
	}
	//
	@Override
	public void run() throws Exception {
		if (server == null) {
			out.println("can not find SipServer.");

			return;
		}
		super.run();
	}
	//
	private void showServerInfo(String args) {
		out.println(server.info());
	}
	
	//
	private void showChannels(String args){
		TablePrinter tp=TablePrinter.create(out)
				.length(15,20,15,40,10,15,10,10)
				.headers("ID",
						"REMOTEADDRESS",
		    			"REMOTEPORT",
		    			"SSHINFO",
		    			"SENTCNT",
		    			"RECECNT",
		    			"CREATETIME",
		    			"CMD");
    	List<WebsockifyChannel>channels=server.getChannels();
    	for(WebsockifyChannel s:channels){
    		tp.print(
        			s.id,
        			s.remoteAddress,
        			s.remotePort,
        			s.messageSentCount,
        			s.messageReceivedCount,
        			formatDate(s.createTime));
    	}
    }
}
