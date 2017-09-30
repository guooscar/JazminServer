/**
 * 
 */
package jazmin.test.driver.mq;

import java.util.Scanner;

import jazmin.core.Jazmin;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.util.RandomUtil;

/**
 * @author yama
 *
 */
public class FileMessageQueueTest {
	public static void main(String[] args) {
		LoggerFactory.setLevel("INFO");
		MessageQueueDriver mq=new MessageQueueDriver();
		Jazmin.addDriver(mq);
		ConsoleServer cs=new ConsoleServer();
		cs.setPort(2222);
		Jazmin.addServer(cs);
		mq.setWorkDir("/Users/yama/Desktop/mq_test");
		//
		TopicQueue queue=mq.createTopicQueue("test1",MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		queue.setMaxTtl(3600*1000*24);
		queue.setRedelieverInterval(1000);
		mq.register(new SimpleSubscriber());
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
			byte payload[]=new byte[RandomUtil.randomInt(500,2000)];
			//for(int i=0;i<RandomUtil.randomInt(70000, 20000000);i++)
			//mq.publish("test1", payload);
			mq.publish("test1",payload);
		}
		
		
		
	}
}
