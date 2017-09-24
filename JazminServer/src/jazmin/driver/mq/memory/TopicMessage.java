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
	public long sendTime;
	public long lastDeliverTime;
	public int deliverTimes;
	public String payloadId;
	public long ttl;
	public String id;
}
