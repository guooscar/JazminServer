/**
 * 
 */
package jazmin.core.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.util.JSONUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

/**
 * @author yama
 * 9 Jun, 2016
 */
public class Monitor extends Lifecycle implements Runnable{
	private static Logger logger=LoggerFactory.get(Monitor.class);
	//
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	private String monitorUrl;
	private Thread monitorThead;
	private List<MonitorAgent>monitorAgents;
	public static final String CATEGORY_TYPE_KV="KeyValue";
	public static final String CATEGORY_TYPE_VALUE="Value";
	public static final String CATEGORY_TYPE_COUNT="Count";
	//
	public Monitor() {
		monitorAgents=new ArrayList<MonitorAgent>();
		registerAgent(new VMMonitorAgent());
		registerAgent(new DispatcherMonitorAgent());
		registerAgent(new OSMonitorAgent());
	}
	//
	public void registerAgent(MonitorAgent agent){
		monitorAgents.add(agent);
	}

	//
	public void sample(String categoryName,String type,Map<String,String>kv){
		String json=JSONUtil.toJson(kv);
		post(categoryName,type,json);
	}
	//
	@Override
	public void start() throws Exception {
		if(monitorUrl==null){
			return;
		}
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setUserAgent("JazminMonitorAgent");
		clientConfigBuilder.setAsyncHttpClientProviderConfig(new NettyAsyncHttpProviderConfig());
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(clientConfig);
		//
		for(MonitorAgent ma:monitorAgents){
			try{
				ma.start(this);
			}catch(Exception e){
				logger.catching(e);
			}
		}
		//
		monitorThead=new Thread(this);
		monitorThead.start();
	}
	//
	@Override
	public void run() {
		int idx=0;
		while(true){
			try {
				Thread.sleep(10*1000L);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
			idx++;
			for(MonitorAgent ma:monitorAgents){
				try{
					ma.sample(idx,this);
				}catch(Exception e){
					logger.catching(e);
				}
			}
		}
	}
	//
	/**
	 * @return the monitorUrl
	 */
	public String getMonitorUrl() {
		return monitorUrl;
	}

	/**
	 * @param monitorUrl the monitorUrl to set
	 */
	public void setMonitorUrl(String monitorUrl) {
		this.monitorUrl = monitorUrl;
	}

	//
	private void post(String name,String type,String data){
		if(monitorUrl==null){
			return;
		}
		String instanceName=Jazmin.getServerName();
		long time=System.currentTimeMillis();
		asyncHttpClient.preparePost(monitorUrl
				+"?instance="+instanceName
				+"&time="+time
				+"&type="+type
				+"&name="+name).setBody(data).execute();
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create().format("%-5s%-30s\n");
		ib.println("monitorUrl:"+monitorUrl);
		ib.section("agents");
		int idx=1;
		for(MonitorAgent ma:monitorAgents){
			ib.print(idx++,ma.getClass().getName());
		}
		return ib.toString();
	}
}
