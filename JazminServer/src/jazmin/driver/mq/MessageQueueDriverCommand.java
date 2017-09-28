package jazmin.driver.mq;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.driver.mq.file.FileTopicQueue;
import jazmin.driver.mq.file.IndexFile;
import jazmin.driver.mq.memory.MemoryTopicQueue;
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
		if(queue instanceof FileTopicQueue){
			FileTopicQueue ft=(FileTopicQueue) queue;
			out.println("length:"+ft.length());
			out.println("IndexSegmentFiles:");
			for(IndexFile f:ft.getIndexSegmentFiles()){
				out.println(f.getIndexFile().getName()+"\tlength:"+f.size()+"\tremoved:"+f.getRemovedCount());
			}
			out.println("TakeSegmentFiles:");
			for(IndexFile f:ft.getTakeSegmentFiles()){
				out.println(f.getIndexFile().getName()+"\tlength:"+f.size()+"\tremoved:"+f.getRemovedCount());
			}
		}
		if (queue instanceof MemoryTopicQueue){
			out.println("length:"+queue.length());
		}
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
		out.println("length:\t"+queue.length());
		out.println("accept:\t"+queue.getAcceptedCount());
		out.println("reject:\t"+queue.getRejectedCount());
		out.println("expired:\t"+queue.getExpiredCount());
		tw.fmagenta();
		chart.reset();
		out.println(chart.draw());
		tw.reset();

	}

	//
    private void showQueue(String args){
    	String format="%-5s : %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n";
		int i=1;
		List<TopicQueue>queues=messageQueueDriver.getTopicQueues();
		out.println("total "+queues.size()+" queues");
		//
		out.format(format,"#","ID","TYPE","LENGTH","PUBLISHED","ACCEPTED","REJECTED","EXPIRED","DELIEVERED");	
		for(TopicQueue q:queues){
			out.format(format,i++,
					q.getId(),
					q.getType(),
					q.length(),
					q.getPublishedCount(),
					q.getAcceptedCount(),
					q.getRejectedCount(),
					q.getExpiredCount(),
					q.getDelieveredCount());
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
