package jazmin.core.monitor;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.misc.io.InvokeStat;

/**
 * 
 * @author yama
 * 10 Jun, 2016
 */
public  class JazminMonitorAgent implements MonitorAgent{
	@Override
	public void start(Monitor monitor) {
		Map<String,String>jazminInfo=new HashMap<String, String>();
		jazminInfo.put("serverName",Jazmin.getServerName());
		jazminInfo.put("serverPath",Jazmin.getServerPath());
		jazminInfo.put("appClassloader",Jazmin.getAppClassLoader()+"");
		jazminInfo.put("applicationPackage",Jazmin.getApplicationPackage());
		jazminInfo.put("jazminVersion",Jazmin.VERSION);
		jazminInfo.put("startTime", new Date()+"");
		Date stopTime=new Date();
		Duration d=Duration.between(stopTime.toInstant(), Jazmin.getStartTime().toInstant());
		jazminInfo.put("uptime",d+"");
		monitor.sample("Jazmin.Info",Monitor.CATEGORY_TYPE_KV,jazminInfo);
	}
	//
	@Override
	public void sample(int idx,Monitor monitor) {
		//sample every 100s 
		if(idx%10==0){
			Map<String,String>info=new HashMap<String, String>();
			List<InvokeStat>stats=Jazmin.dispatcher.getInvokeStats();
			for(InvokeStat stat:stats){
				String s=("IC:"+stat.invokeCount+
						  " ERROR:"+stat.errorCount+
						  " AVGFULL:"+stat.avgFullTime()+
						  " AVGRUN:"+stat.avgRunTime()+
						  " MAXFULL:"+stat.maxFullTime+
						  " MAXRUN:"+stat.maxRunTime);
				info.put(stat.name, s);
			};
			monitor.sample("Jazmin.InvokeStat",Monitor.CATEGORY_TYPE_KV,info);
		}
		
	}
}