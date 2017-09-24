package jazmin.driver.mq;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
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
    	addOption("queue",false,"show queue information.",this::showQueue);
    	addOption("subscriber",false,"show subscriber information.",this::showSubscriber);
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
	
	//
    private void showQueue(String args){
    	String format="%-5s : %-20s %-20s %-10s \n";
		int i=1;
		List<TopicQueue>queues=messageQueueDriver.getTopicQueues();
		out.println("total "+queues.size()+" queues");
		//
		out.format(format,"#","ID","TYPE","LENGTH");	
		for(TopicQueue q:queues){
			
			out.format(format,i++,
					q.getId(),
					q.getType(),
					q.length());
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
		out.format(format,"#","ID","TOPIC","SENTCOUNT","LASTSENTTIME");	
		SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(TopicSubscriber q:queues){
			out.format(format,i++,
					q.id,
					q.topic,
					q.sentCount,
					sdf.format(new Date(q.lastSentTime)));
		};
    }
    
	//
    private void showDriverInfo(String args)throws Exception{
    	out.println(messageQueueDriver.info());
    }
   
    
}
