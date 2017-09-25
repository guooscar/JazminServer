/**
 * 
 */
package jazmin.test.driver.mq;

import jazmin.driver.mq.MessageEvent;
import jazmin.driver.mq.TopicSubscriberDefine;

/**
 * @author yama
 *
 */
public class SimpleSubscriber {
	//
	@TopicSubscriberDefine(topic="test1",name=1)
	public void test1(MessageEvent e){
		System.err.println("test1:"+e.message.delieverTimes+"-"+e.message.payload);
		e.messageQueueDriver.accept("test1",e.message.id);
	}
	
	@TopicSubscriberDefine(topic="test1",name=2)
	public void test12(MessageEvent e){
		System.err.println("test11:"+e.message.delieverTimes+"-"+e.message.payload);
		e.messageQueueDriver.reject("test1",e.message.id);
		if(e.message.delieverTimes>5){
			e.messageQueueDriver.accept("test1",e.message.id);
		}
	}
	
}
