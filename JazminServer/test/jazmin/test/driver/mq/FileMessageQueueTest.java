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
		try{
			test();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//
	private static void test(){
		LoggerFactory.setLevel("WARN");
		MessageQueueDriver mq=new MessageQueueDriver();
		Jazmin.addDriver(mq);
		ConsoleServer cs=new ConsoleServer();
		cs.setPort(2222);
		Jazmin.addServer(cs);
		mq.setWorkDir("/Users/yama/Desktop/mq_test");
		//
		TopicQueue queue1=mq.createTopicQueue("test1",MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		TopicQueue queue2=mq.createTopicQueue("test2",MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		
		mq.register(new SimpleSubscriber());
		//
		Jazmin.start();
		Scanner sc=new Scanner(System.in);
		while(true){
			sc.nextInt();
			new Thread(){
				public void run() {
					byte payload[]=new byte[RandomUtil.randomInt(100,2000)];
					for(int i=0;i<RandomUtil.randomInt(70000, 20000000);i++)
					mq.publish("test1", payload);
				};
			}.start();
			//
			new Thread(){
				public void run() {
					byte payload[]=new byte[50];
					for(int i=0;i<RandomUtil.randomInt(70000, 20000000);i++)
					mq.publish("test2", payload);
				};
			}.start();
		}
	}
}
