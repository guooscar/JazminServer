package jazmin.server.msg;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class KcpChannelCommand extends ConsoleCommand {
    private KcpChannelManager channelManager;
	public KcpChannelCommand() {
    	super(true);
    	id="kcp";
    	desc="kcp channel ctrl command";
    	addOption("s",false,"show all sessions.",this::showSessions);
    	//
    	MessageServer ms=Jazmin.getServer(MessageServer.class);
    	if(ms!=null){
    		channelManager=ms.kcpChannelManager;
    	}
    }
	//
	@Override
	public void run() throws Exception{
		 if(channelManager==null){
			 out.println("can not find channelManager.");
			 return;
		 }
		 super.run();
	}
	private void showSessions(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(12,16,7,7,10,15,15,15,15,15)
    			.headers("CONVID",
    					"HOST",
    					"PORT",
    					"RECEIVE",
    					"SENT",
    					"LASTSEND",
    					"LASTRECV",
    					"CREATETIME",
    					"PEERTIME",
    					"LAG");
    	
		List<KcpChannel> sessions=channelManager.getChannels();
		//
		SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(KcpChannel s:sessions){
			String host="";
			int port=0;
			if(s.getRemoteAddress()!=null){
				host=s.getRemoteAddress().getAddress().getHostAddress();
				port=s.getRemoteAddress().getPort();
			}
			long totalPingPkgCount=s.totalPingPkgCount;
			if(s.totalPingPkgCount==0){
				totalPingPkgCount=1;
			}
			tp.print(
					s.getConvId(),
					host,
					port,
					s.receivePacketCount,
					s.sentPacketCount,
					sdf.format(new Date(s.lastSentTime)),
					sdf.format(new Date(s.lastReceiveTime)),
					sdf.format(new Date(s.createTime)),
					sdf.format(new Date(s.peerTimestamp)),
					s.lag+"/"+(s.totalLag/totalPingPkgCount)+"ms");
		};
    }
}
