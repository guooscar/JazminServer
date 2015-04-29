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
		out.printf(format,"maxStartPort",server.getMaxStartPort());
		out.printf(format,"minStartPort",server.getMinStartPort());
	}
    //
    private void showChannels(String args){
    	List<RelayChannel>channels=server.getChannels();
    	out.format("total %d channels\n",channels.size());
    	String format="%-5s %-15s %-65s %-20s %-20s %-15s %-15s\n";
    	out.printf(format,
				"#",
				"NAME",
    			"INFO",
    			"PEERPACKET-A",
    			"PEERPACKET-B",
    			"CREATETIME",
    			"LASTACCTIME");
    	int idx=1;
    	for(RelayChannel sh:channels){
    		double byteA=sh.peerAByteCount;
    		double byteB=sh.peerBByteCount;
    		out.printf(format,
        			idx++,
        			sh.getName(),
        			sh.toString(),
        			sh.peerAPacketCount+"/"+String.format("%.2fKB",byteA/1024),
        			sh.peerBPacketCount+"/"+String.format("%.2fKB",byteB/1024),
        			formatDate(new Date(sh.createTime)),
        			formatDate(new Date(sh.lastAccessTime)));
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
