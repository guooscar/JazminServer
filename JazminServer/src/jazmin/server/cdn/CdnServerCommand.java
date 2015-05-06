package jazmin.server.cdn;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class CdnServerCommand extends ConsoleCommand {
	private CdnServer server;

	public CdnServerCommand() {
		super();
		id = "cdnsrv";
		desc = "cdn server ctrl command";
		addOption("i", false, "show server information.", this::showServerInfo);
		addOption("r", false, "show file requests.", this::showRequests);
		//
		server = Jazmin.getServer(CdnServer.class);
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
		out.printf(format, "homeDir", server.getHomeDir());
	}
	
	private void showRequests(String args){
    	List<FileRequest>requests=server.getFileRequests();
    	out.format("total %d requests\n",requests.size());
    	String format="%-5s %-10s %-20s %-20s %-15s %-30s\n";
    	out.printf(format,
				"#",
				"ID",
    			"REMOTEADDRESS",
    			"TRANS RATE",	
    			"CREATETIME",
    			"URI");
    	int idx=1;
    	for(FileRequest r:requests){
    		out.printf(format,
        			idx++,
        			r.id,
        			r.remoteAddress.getAddress().getHostAddress()+":"+r.remoteAddress.getPort(),
        			r.transferedBytes+"/"+r.totalBytes,
        			formatDate(r.createTime),
        			r.uri);
    	}
    }
}
