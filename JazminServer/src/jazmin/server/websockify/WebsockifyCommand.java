package jazmin.server.websockify;

import java.net.InetSocketAddress;
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
			out.println("can not find WebsockifyServer.");
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
				.length(10,30,30,10,10,15)
				.headers("ID",
						"INBOUND",
		    			"OUTBOUND",
		    			"SENTCNT",
		    			"RECECNT",
		    			"CREATETIME");
    	List<WebsockifyChannel>channels=server.getChannels();
    	for(WebsockifyChannel s:channels){
    		String in="";
    		if(s.inBoundChannel!=null){
    			InetSocketAddress is=(InetSocketAddress) s.inBoundChannel.remoteAddress();
    			in=is.getAddress().getHostAddress()+":"+is.getPort();
    		}
    		tp.print(
        			s.id,
        			in,
        			s.remoteAddress+":"+s.remotePort,
        			s.messageSentCount,
        			s.messageReceivedCount,
        			formatDate(s.createTime));
    	}
    }
}
