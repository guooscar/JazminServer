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
	@TopicSubscriberDefine(topic="test1",name=2)
	public void test1(MessageEvent e){
		System.err.println("test12:"+e.message);
		if(RandomUtil.randomInt(5)==1){
			e.messageQueueDriver.reject(e.message);
		}else{
			e.messageQueueDriver.accept(e.message);
		}
		
	}
	//
	@TopicSubscriberDefine(topic="test1",name=1)
	public void reject(MessageEvent e){
		System.err.println("test11:"+e.message);
		e.messageQueueDriver.accept(e.message);
	}
	
}
