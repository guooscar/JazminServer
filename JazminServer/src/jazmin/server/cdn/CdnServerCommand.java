package jazmin.server.cdn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.util.DumpUtil;

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
		out.printf(format,"port",server.getPort());
		out.printf(format,"homeDir",server.getHomeDir());
		out.printf(format,"orginSiteURL",server.getOrginSiteURL());
		out.printf(format,"listDir",server.isListDir());
		out.printf(format,"listDirInHtml",server.isListDirInHtml());
		out.printf(format,"requestFilter",server.getRequestFilter());
		
		for(Entry<String,Long> e:server.getPolicyMap().entrySet()){
			out.printf(format,"policy-"+e.getKey(),e.getValue());
		}
	}
	//
	private Map<String,Long>lastTransferBytes=new HashMap<String, Long>();
	//
	private void showRequests(String args){
    	List<FileRequest>requests=server.getFileRequests();
    	out.format("total %d requests\n",requests.size());
    	String format="%-5s %-10s %-20s %-20s %-15s %-15s %-10s %-30s\n";
    	out.printf(format,
				"#",
				"ID",
    			"REMOTEADDRESS",
    			"TRANS",
    			"RATE",
    			"CREATETIME",
    			"SOURCE",
    			"URI");
    	int idx=1;
    	long totalRate=0;
    	for(FileRequest r:requests){
    		//
    		long lastBytes=lastTransferBytes.getOrDefault(r.id,r.transferedBytes);
    		String transfer=DumpUtil.byteCountToString(r.transferedBytes)+"/"+
    				DumpUtil.byteCountToString(r.totalBytes);
    		long currentRate=r.transferedBytes-lastBytes;
    		String rate=DumpUtil.byteCountToString(currentRate)+"/s";
    		totalRate+=currentRate;
    		//
    		out.printf(format,
        			idx++,
        			r.id,
        			r.remoteAddress.getAddress().getHostAddress()+":"+r.remoteAddress.getPort(),
        			transfer,
        			rate,
        			formatDate(r.createTime),
        			r.sourceType,
        			r.uri);
    		lastTransferBytes.put(r.id,r.transferedBytes);
    	}
    	//
    	out.println("total rate:"+DumpUtil.byteCountToString(totalRate)+"/s");
    }
}
