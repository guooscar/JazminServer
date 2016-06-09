/**
 * 
 */
package jazmin.test.core;

import jazmin.core.Jazmin;

/**
 * @author yama
 * 9 Jun, 2016
 */
public class MonitorTest {
	public static void main(String[] args) {
		Jazmin.mointor.setMonitorUrl("http://skydu.local:7001/srv/monitor/report");
		Jazmin.start();
	}
}
