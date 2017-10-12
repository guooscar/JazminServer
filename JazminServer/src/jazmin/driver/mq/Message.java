/**
 * 
 */
package jazmin.driver.mq;

/**
 * @author yama
 *
 */
public class Message {
	public long id;
	public String topic;
	public short subscriber;
	public Object payload;
	public int delieverTimes;
	//
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Message [id=" + id + ", topic=" + topic + ", subscriber=" + subscriber + ", delieverTimes="
				+ delieverTimes + "]";
	}
	
}
