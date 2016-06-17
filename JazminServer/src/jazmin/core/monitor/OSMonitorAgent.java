/**
 * 
 */
package jazmin.core.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 * 9 Jun, 2016
 */
public class OSMonitorAgent implements MonitorAgent{
	private OperatingSystemMXBean osMXBean;
	//
	@Override
	public void start(Monitor monitor) {
		osMXBean=ManagementFactory.getOperatingSystemMXBean();
    	//
		Map<String,String>vmInfo=new HashMap<String, String>();
		vmInfo.put("Name",osMXBean.getName());
		vmInfo.put("Arch",osMXBean.getArch());
		vmInfo.put("Version",osMXBean.getVersion());
		vmInfo.put("AvailableProcessors",osMXBean.getAvailableProcessors()+"");
		monitor.sample("OS.Info",Monitor.CATEGORY_TYPE_KV,vmInfo);
	}

	@Override
	public void sample(int idx,Monitor monitor) {
		Map<String,String>loadAvg=new HashMap<String, String>();
		loadAvg.put("SystemLoadAverage",((int)(osMXBean.getSystemLoadAverage()*1000))+"");
		monitor.sample("OS.SystemLoadAverage",Monitor.CATEGORY_TYPE_VALUE,loadAvg);
	}
	
}
