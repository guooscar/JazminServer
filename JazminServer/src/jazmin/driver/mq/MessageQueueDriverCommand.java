package jazmin.driver.mq;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.AsciiChart;
import jazmin.server.console.ascii.TerminalWriter;
import jazmin.server.console.builtin.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class MessageQueueDriverCommand extends ConsoleCommand {
    private MessageQueueDriver messageQueueDriver;
	public MessageQueueDriverCommand() {
    	super(true);
    	id="mq";
    	desc="message queue driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("queue",false,"show queues.",this::showQueue);
    	addOption("subscriber",false,"show subscribers.",this::showSubscriber);
    	addOption("queuetps",true,"show queue publish tps .",this::showQueuePublishTps);
    	addOption("queueinfo",true,"show queue info .",this::showQueueInfo);
    	//
    	messageQueueDriver=Jazmin.getDriver(MessageQueueDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(messageQueueDriver==null){
			 out.println("can not find MessageQueueDriver.");
			 return;
		 }
		 super.run();
	}
	private long lastPublishCount=0;
	private long maxPublishCount;
	//
	private void showQueuePublishTps(String args) throws Exception {
		TopicQueue queue=messageQueueDriver.getTopicQueue(args);
		if(queue==null){
			out.println("can not find topic queue :"+args);
			return;
		}
		TerminalWriter tw = new TerminalWriter(out);
		AsciiChart chart = new AsciiChart(160, 80);
		lastPublishCount = queue.getPublishedCount();
		while (stdin.available() == 0) {
			tw.cls();
			out.println("press any key to quit.");
			showQueueTps(chart, tw,queue);
			out.flush();
			TimeUnit.SECONDS.sleep(1);
		}
		stdin.read();
	}
	//
	private void showQueueInfo(String args) throws Exception {
		TopicQueue queue=messageQueueDriver.getTopicQueue(args);
		if(queue==null){
			out.println("can not find topic queue :"+args);
			return;
		}
		out.println("id:"+queue.id);
		out.println("type:"+queue.type);
		out.println("maxttl:"+queue.getMaxTtl());
		out.println("redeliever interval:"+queue.getRedelieverInterval());
		
	}
	//
	private void showQueueTps(AsciiChart chart, TerminalWriter tw,TopicQueue queue) {
		long invokeCount = queue.getPublishedCount();
		long invokeTps = invokeCount - lastPublishCount;
		if (invokeTps > maxPublishCount) {
			maxPublishCount = invokeTps;
		}
		chart.addValue((int) (invokeTps));
		lastPublishCount = invokeCount;
		//
		out.println("-----------------------------------------------------");
		out.println("queue ["+queue.id+"] publish tps chart. total:" + invokeCount 
					+ " max:" + maxPublishCount + " current:"
					+ invokeTps + "/s");
		tw.fmagenta();
		chart.reset();
		out.println(chart.draw());
		tw.reset();

	}

	//
    private void showQueue(String args){
    	String format="%-5s %-25s %-6s %-10s %-10s %-10s %-10s %-10s %-10s\n";
		int i=1;
		List<TopicQueue>queues=messageQueueDriver.getTopicQueues();
		out.println("total "+queues.size()+" queues");
		//
		out.format(format,"#","ID","TYPE","PUBLISHED","LENGTH","ACCEPTED","REJECTED","DELIEVERED","EXPIRED");	
		for(TopicQueue q:queues){
			out.format(format,i++,
					q.getId(),
					q.getType(),
					q.getPublishedCount(),"","","","","");
			//
			for(TopicChannel tc:q.getChannels()){
				out.format(format,
						"",
						" >"+tc.getSubscriber().name+"["+tc.getSubscriber().id+"]","","",
						tc.length(),
						tc.getAcceptedCount(),
						tc.getRejectedCount(),
						tc.getDelieveredCount(),
						tc.getExpiredCount());
			}
		};
    }
    //
    
    //
    private void showSubscriber(String args){
    	String format="%-5s : %-30s %-20s %-10s %-20s\n";
		int i=1;
		List<TopicSubscriber>queues=messageQueueDriver.getTopicSubscribers();
		out.println("total "+queues.size()+" subscriber");
		//
		out.format(format,"#","ID","TOPIC","DELIEVER-COUNT","LAST-DELIEVER");	
		SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(TopicSubscriber q:queues){
			out.format(format,i++,
					q.id,
					q.topic,
					q.delieverCount,
					sdf.format(new Date(q.lastDelieverTime)));
		};
    }
    
	//
    private void showDriverInfo(String args)throws Exception{
    	out.println(messageQueueDriver.info());
    }
   
    
}
