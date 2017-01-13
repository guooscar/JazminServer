package jazmin.server.rtmp;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.server.rtmp.rtmp.RtmpPublisher;
import jazmin.util.DumpUtil;

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
    	super(true);
    	id="rtmpsrv";
    	desc="rtmp server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("app",false,"show server applications.",this::showApplication);
    	addOption("handler",false,"show server handlers.",this::showHandlers);
    	addOption("publisher",false,"show server handler publisher.",this::showHandlerPublisher);
    	//
    	server=Jazmin.getServer(RtmpServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(server==null){
			 out.println("can not find RtmpServer.");
			 
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	out.println(server.info());
	}
    //
    private void showHandlerPublisher(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(10,10,20,10,6,6,10,6,15,10,10,10)
    			.headers("CLIENTID",
    	    			"PLAYNAME",
    	    			"REMOTEHOST",
    	    			"STREAMID",
    	    			"BD",
    	    			"CC",
    	    			"PLAYLEN",
    	    			"SEEK",
    	    			"STARTTIME",
    	    			"TIMEPOS",
    	    			"TIMETICK",
    	    			"CREATETIME");
    	List<ServerHandler>handlers=server.getHandlers();
    	for(ServerHandler sh:handlers){
    		RtmpPublisher p=sh.getPublisher();
    		tp.print(
        			sh.getClientId(),
        			sh.getPlayName(),
        			sh.getRemoteHost()+":"+sh.getRemotePort(),
        			sh.getStreamId(),
					p == null ? "NULL" : p.getBufferDuration(),
					p == null ? "NULL" : p.getCurrentConversationId(),
					p == null ? "NULL" : p.getPlayLength(),
					p == null ? "NULL" : p.getSeekTime(),
					p == null ? "NULL" : formatDate(new Date(p.getStartTime())),
					p == null ? "NULL" : p.getTimePosition(),
					p == null ? "NULL" : p.getTimerTickSize(),
					formatDate(sh.getCreateTime()));
    	}
    }
    //
    private void showHandlers(String args){
    	showHandlers(args, null);
    }
    //
    private void showHandlers(String args,Map<String,Long>lastValue){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(15,10,20,10,10,20,20,15)
    			.headers("CLIENTID",
    	    			"PLAYNAME",
    	    			"REMOTEHOST",
    	    			"STREAMID",
    	    			"BUFFERDURA",
    	    			"R/W",
    	    			"R/W RATE",
    	    			"CREATETIME");
    	List<ServerHandler>handlers=server.getHandlers();
    	for(ServerHandler sh:handlers){
    		String rate="--";
    		if(lastValue!=null){
    			long lastRead=lastValue.getOrDefault(sh.getClientId()+"R",0L);
    			long lastWritten=lastValue.getOrDefault(sh.getClientId()+"W",0L);
    			long rateR=sh.getBytesRead()-lastRead;
    			long rateW=sh.getBytesWritten()-lastWritten;
    			//
    			rate=DumpUtil.byteCountToString(rateR)+"/"+DumpUtil.byteCountToString(rateW);
    			//
    			lastValue.put(sh.getClientId()+"R", sh.getBytesRead());
    			lastValue.put(sh.getClientId()+"W", sh.getBytesWritten());
    		}
    		tp.print(
        			sh.getClientId(),
        			sh.getPlayName(),
        			sh.getRemoteHost()+":"+sh.getRemotePort(),
        			sh.getStreamId(),
        			sh.getBufferDuration(),
        			sh.getBytesRead()+"/"+sh.getBytesWritten(),
        			rate,
        			formatDate(sh.getCreateTime()));
    	}
    }
  
    //
    static class StreamBean{
    	public String name;
    	public String publishType;
    	public String host;
    	public int port;
    	public Date createTime;
    	public Map<String,String> metadata;
    	public List<SubscriberBean>subscribes;
    }
    //
    static class SubscriberBean{
    	public String host;
    	public int port;
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
				StreamBean sb=new StreamBean();
				sb.name=ss.getName();
				sb.createTime=ss.getCreateTime();
				Channel publisher=ss.getPublisher();
				if(publisher!=null){
					InetSocketAddress sa=(InetSocketAddress) ss.getPublisher().getRemoteAddress();
					sb.publishType=ss.getPublishType().asString();
					sb.host=sa.getAddress().getHostAddress();
					sb.port=sa.getPort();
				}
				sb.metadata=ss.getMetadata();
				sb.subscribes=new ArrayList<RtmpServerCommand.SubscriberBean>();
				ChannelGroup cgs=ss.getSubscribers();
				if(cgs!=null){
					for(Channel c:cgs){
						SubscriberBean subBean=new SubscriberBean();
						InetSocketAddress qq=(InetSocketAddress)c.getRemoteAddress();
						subBean.host=qq.getAddress().getHostAddress();
						subBean.port=qq.getPort();
						sb.subscribes.add(subBean);
					}	
				}
				out.println("Stream-"+streamIdx++ +"------->\n"+DumpUtil.dump(sb));
			}
			printLine('=', 80);
		};
    }
}
