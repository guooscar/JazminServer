/**
 * 
 */
package jazmin.driver.mq.memory;

/**
 * @author yama
 *
 */
public class TopicMessage {
	public short subscriber;
	public long lastDeliverTime;
	public int deliverTimes;
	public String payloadId;
	public long id;
	//
	public TopicMessage next;
	public TopicMessage prior;
}
