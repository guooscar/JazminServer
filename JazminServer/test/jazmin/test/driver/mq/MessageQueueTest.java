/**
 * 
 */
package jazmin.test.driver.mq;

import java.util.Scanner;

import jazmin.core.Jazmin;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class MessageQueueTest {
	public static void main(String[] args) {
		LoggerFactory.setLevel("INFO");
		MessageQueueDriver mq=new MessageQueueDriver();
		Jazmin.addDriver(mq);
		ConsoleServer cs=new ConsoleServer();
		cs.setPort(2222);
		Jazmin.addServer(cs);
		mq.setWorkDir("/Users/yama/Desktop/mq_test");
		//
		mq.createTopic("test1",MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		//mq.createTopic("test1",MessageQueueDriver.TOPIC_QUEUE_TYPE_MEMORY);
		mq.subscribe(new SimpleSubscriber());
		//
		Jazmin.start();
		/*for(int i=0;i<1000;i++){
			try {
				Thread.sleep(new Random().nextInt(1000));
				mq.publish("test1", "xxxx");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}*/
		Scanner sc=new Scanner(System.in);
		while(true){
			sc.nextInt();
			for(int i=0;i<1;i++)
			mq.publish("test1", "xxxaddddddddddddddddddddddddx".getBytes());
		}
		
		
		
	}
}
