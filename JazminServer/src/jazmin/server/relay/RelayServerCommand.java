package jazmin.server.relay;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RelayServerCommand extends ConsoleCommand {
    private RelayServer server;
	public RelayServerCommand() {
    	super();
    	id="relaysrv";
    	desc="relay server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("c",false,"show server channels.",this::showChannels);
    	addOption("ctop",false,"show server channels.",this::showChannelsTop);
    	//
    	server=Jazmin.getServer(RelayServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(server==null){
			 err.println("can not find RtmpServer.");
			 
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	String format="%-25s: %-10s\n";
		out.printf(format,"hostAddress",server.getHostAddress());
		out.printf(format,"idleTime",server.getIdleTime());
		out.printf(format,"maxBindPort",server.getMaxBindPort());
		out.printf(format,"minBindPort",server.getMinBindPort());
		out.printf(format,"hostAddresses",server.getHostAddresses());
		
	}
    //
    private void showChannels(String args){
    	List<RelayChannel>channels=server.getChannels();
    	out.format("total %d channels\n",channels.size());
    	String format="%-5s %-6s %-6s %-10s %-6s %-50s %-20s %-20s %-15s %-15s %-15s\n";
    	out.printf(format,
				"#",
				"ID",
				"TRSNP",
    			"NAME",
    			"ACTIVE",
    			"LINK",
    			"PKG SENT",
    			"PKG RECV",
    			"CREATETIME",
    			"LASTACCTIME",
    			"LINKED");
    	int idx=1;
    	for(RelayChannel sh:channels){
    		double byteSentCnt=sh.byteSentCount;
    		double byteReveCnt=sh.byteReceiveCount;
    		StringBuilder linkedStr=new StringBuilder();
    		linkedStr.append("[");
    		for(RelayChannel linked:sh.linkedChannels){
    			linkedStr.append(linked.id+",");
    		}
    		linkedStr.append("]");
    		String remoteAddressStr="";
    		if(sh.remoteAddress!=null){
    			remoteAddressStr=sh.remoteAddress.getAddress().getHostAddress()
    					+":"+sh.remoteAddress.getPort();
    		}
    		out.printf(format,
        			idx++,
        			sh.id,
        			sh.transportType,
        			sh.name,
        			sh.isActive(),
        			sh.localHostAddress+":"+sh.localPort+"<-->"+remoteAddressStr,
        			sh.packetSentCount+"/"+String.format("%.2fKB",byteSentCnt/1024),
        			sh.packetReceiveCount+"/"+String.format("%.2fKB",byteReveCnt/1024),
        			formatDate(new Date(sh.createTime)),
        			formatDate(new Date(sh.lastAccessTime)),
        			linkedStr);		
    	}
    }
   
    //
    private void showChannelsTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showChannels(args);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    }
    
}
