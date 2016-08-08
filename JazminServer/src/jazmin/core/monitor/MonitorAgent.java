/**
 * 
 */
package jazmin.core.monitor;

/**
 * @author yama
 * 9 Jun, 2016
 */
public interface MonitorAgent {
	public void start(Monitor monitor);
	public void sample(int idx,Monitor monitor);
}
