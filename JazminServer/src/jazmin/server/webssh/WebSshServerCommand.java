package jazmin.server.webssh;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class WebSshServerCommand extends ConsoleCommand {
	private WebSshServer server;

	public WebSshServerCommand() {
		super(true);
		id = "webssh";
		desc = "webssh server ctrl command";
		addOption("i", false, "show server information.", this::showServerInfo);
		addOption("c", false, "show server channels.", this::showChannels);
		//
		server = Jazmin.getServer(WebSshServer.class);
	}
	//
	@Override
	public void run() throws Exception {
		if (server == null) {
			out.println("can not find WebSshServer.");

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
    	List<WebSshChannel>channels=server.getChannels();
    	for(WebSshChannel s:channels){
    		tp.print(
        			s.id,
        			s.remoteAddress,
        			s.remotePort,
        			s.hostInfo.user+"@"+s.hostInfo.host+":"+s.hostInfo.port,
        			s.messageSentCount,
        			s.messageReceivedCount,
        			formatDate(s.createTime),
        			s.hostInfo.cmd);
    	}
    }
}
