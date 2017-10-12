/**
 * 
 */
package jazmin.test.driver.mq;

import jazmin.driver.mq.MessageEvent;
import jazmin.driver.mq.TopicSubscriberDefine;
import jazmin.util.RandomUtil;

/**
 * @author yama
 *
 */
public class SimpleSubscriber {
	//
	@TopicSubscriberDefine(topic="test1",id=2)
	public void test1(MessageEvent e){
		//System.err.println("test12:"+e.message);
		if(RandomUtil.randomInt(5)==1){
			e.messageQueueDriver.reject(e.message);
		}else{
			e.messageQueueDriver.accept(e.message);
		}
		
	}
	//
	@TopicSubscriberDefine(topic="test1",id=1)
	public void reject(MessageEvent e){
		//System.err.println("test11:"+e.message);
		e.messageQueueDriver.accept(e.message);
	}
	//
	@TopicSubscriberDefine(topic="test1",id=3)
	public void slow(MessageEvent e){
		//System.err.println("test11:"+e.message);
		e.messageQueueDriver.accept(e.message);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	//
	//
	@TopicSubscriberDefine(topic="test2",id=1)
	public void accept(MessageEvent e){
		e.messageQueueDriver.accept(e.message);
	}
}
