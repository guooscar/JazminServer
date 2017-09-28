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
	public short subscriber;
	public Object payload;
	public int delieverTimes;
}
