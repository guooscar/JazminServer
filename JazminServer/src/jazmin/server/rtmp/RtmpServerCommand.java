package jazmin.server.rtmp;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
import jazmin.server.rtmp.rtmp.server.ServerApplication;
import jazmin.server.rtmp.rtmp.server.ServerHandler;
import jazmin.server.rtmp.rtmp.server.ServerStream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RtmpServerCommand extends ConsoleCommand {
    private RtmpServer server;
	public RtmpServerCommand() {
    	super();
    	id="rtmpsrv";
    	desc="rtmp server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("app",false,"show server applications.",this::showApplication);
    	addOption("handler",false,"show server handlers.",this::showHandlers);
    	addOption("handlertop",false,"show server handlers.",this::showHandlerTop);
    	//
    	server=Jazmin.getServer(RtmpServer.class);
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
		out.printf(format,"port",server.getPort());
		out.printf(format,"idleTimeout",server.getServerHome());
	}
    //
    private void showHandlers(String args){
    	List<ServerHandler>handlers=server.getHandlers();
    	out.format("total %d handlers\n",handlers.size());
    	String format="%-5s %-15s %-10s %-20s %-10s %-10s %-10s %-10s %-15s\n";
    	out.printf(format,
				"#",
    			"CLIENTID",
    			"PLAYNAME",
    			"REMOTEHOST",
    			"STREAMID",
    			"BUFFERDURA",
    			"BYTE READ",
    			"BYTE WRITTEN",
    			"CREATETIME");
    	int idx=1;
    	for(ServerHandler sh:handlers){
    		out.printf(format,
        			idx++,
        			sh.getClientId(),
        			sh.getPlayName(),
        			sh.getRemoteHost()+":"+sh.getRemotePort(),
        			sh.getStreamId(),
        			sh.getBufferDuration(),
        			sh.getBytesRead(),
        			sh.getBytesWritten(),
        			formatDate(sh.getCreateTime()));
    	}
    }
    //
    private void showHandlerTop(String args)throws Exception{
    	TerminalWriter tw=new TerminalWriter(out);
    	while(stdin.available()==0){
    		tw.cls();
    		out.println("press any key to quit.");
    		showHandlers(args);
    		out.flush();
    		TimeUnit.SECONDS.sleep(1);
    	}
    }
    //
    private void showApplication(String args){
    	List<ServerApplication>apps=server.getApplications();
    	out.format("total %d applications\n",apps.size());
    	int idx=1;
    	for(ServerApplication a:apps){
			out.println("Applicaiton-"+(idx++) +":\t"+a.getName()
					+"@"+formatDate(a.getCreateTime()));
			int streamIdx=1;
			for(ServerStream ss:a.getStreams()){
				out.println("Stream-"+(streamIdx++)+":\t"+ss.getName()
						+"/"+ss.getPublishType()+"/"+ss.getPublisher()
						+"/"+formatDate(ss.getCreateTime()));
				ChannelGroup cgs=ss.getSubscribers();
				if(cgs!=null){
					int channelIdx=1;
					for(Channel c:cgs){
						out.println("Subscriber-"+(channelIdx++)+":\t"+c+"/"
									+formatDate(ss.getCreateTime()));
					}	
				}
			}
			printLine('=', 80);
		};
    }
}
