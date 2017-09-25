/**
 * 
 */
package jazmin.driver.mq.memory;

/**
 * @author yama
 *
 */
public class TopicMessage {
	public String subscriber;
	public long lastDeliverTime;
	public int deliverTimes;
	public String payloadId;
	public String id;
}
