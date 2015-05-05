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
    	addOption("dump",true,"relay channel to hex dump channel ",this::dumpChannel);
    	addOption("undump",true,"remove hex dump channel ",this::unDumpChannel);
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
    	String format="%-5s %-6s %-20s %-6s %-20s %-15s %-15s %-15s %-50s\n";
    	out.printf(format,
				"#",
				"ID",
				"NAME",
    			"ACTIVE",
    			"PKG RELAY",
    			"CREATETIME",
    			"LASTACCTIME",
    			"LINKED",
    			"LINK");
    	int idx=1;
    	for(RelayChannel sh:channels){
    		double byteRelayCnt=sh.byteRelayCount;
    		StringBuilder linkedStr=new StringBuilder();
    		linkedStr.append("[");
    		for(RelayChannel linked:sh.linkedChannels){
    			linkedStr.append(linked.id+",");
    		}
    		linkedStr.append("]");
    		//
    		out.printf(format,
        			idx++,
        			sh.id,
        			sh.name,
        			sh.isActive(),
        			sh.packetRelayCount+"/"+String.format("%.2fKB",byteRelayCnt/1024),
        			formatDate(new Date(sh.createTime)),
        			formatDate(new Date(sh.lastAccessTime)),
        			linkedStr,
        			sh.getInfo());	
    		printLine('-', environment.getColumns());
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
    //
    private void dumpChannel(String cid){
    	RelayChannel rc=server.getChannel(cid);
    	if(rc==null){
    		err.println("can not found channel with id:"+cid);
    		return;
    	}
    	//
    	HexDumpRelayChannel hexDump=new HexDumpRelayChannel();
    	server.addChannel(hexDump);
    	rc.relayTo(hexDump);
    }
    //
    private void unDumpChannel(String cid){
    	RelayChannel rc=server.getChannel(cid);
    	if(rc==null){
    		err.println("can not found channel with id:"+cid);
    		return;
    	}
    	//
    	rc.getLinkedChannels().forEach(c->{
    		if(c instanceof HexDumpRelayChannel){
    			rc.unRelay(c);
    		}
    	});
    }
}
