/**
 * 
 */
package jazmin.core.thread;

import java.util.Date;

/**
 * @author yama
 * 3 Sep, 2015
 */
public class PerformanceLog {
	public Date date;
	public int poolSize;
	public int queueSize;
	public double avgFullTime;
	public double avgRunTime;
	public long rejectedCount;
	public long invokeCount;
	public long submitCount;
}
