/**
 * 
 */
package jazmin.driver.mq;

/**
 * @author yama
 *
 */
public class Message {
	public String id;
	public short subscriber;
	public Object payload;
	public int delieverTimes;
}
