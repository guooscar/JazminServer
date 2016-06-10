/**
 * 
 */
package jazmin.deploy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jazmin.deploy.domain.MonitorInfo;
import jazmin.deploy.domain.MonitorInfoQuery;
import jazmin.deploy.view.monitor.MonitorManager;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;
import jazmin.util.JSONUtil;

/**
 * 
 * @author icecooly
 *
 */
@Controller(id = "monitor")
public class MonitorController {
	//
	@Service(id = "report", method = HttpMethod.ALL)
	public void report(Context c) {
		MonitorInfo info = new MonitorInfo();
		info.instance = c.getString("instance");
		info.time = c.getLong("time");
		info.type = c.getString("type");
		info.name = c.getString("name");
		info.value = c.request().body();
		MonitorManager.get().addMonitorInfo(info);
		c.view(new JsonView());
	}

	@Service(id = "view")
	public void monitorView(Context c) {
		String instance = c.getString("instance", true);
		String keyvalues = c.getStringOrDefault("keyvalues", "");
		String charts = c.getStringOrDefault("charts", "");
		Set<String> inclues = new TreeSet<>();
		String[] kvs = keyvalues.split("\\$");
		String[] cs = charts.split("\\$");
		for (String temp : kvs) {
			inclues.add(temp);
		}
		for (String temp : cs) {
			inclues.add(temp);
		}
		List<MonitorInfo> list = MonitorManager.get().getMonitorInfos(instance);
		Iterator<MonitorInfo> iterator = list.iterator();
		while (iterator.hasNext()) {
			MonitorInfo info = iterator.next();
			if (inclues.contains(info.name)) {
				continue;
			}
			iterator.remove();
		}
		list.sort((a,b)->{
			return a.name.indexOf(0)-b.name.indexOf(0);
		});
		c.put("list", list);
		c.view(new ResourceView("/jsp/monitor.jsp"));
	}

	@Service(id = "refresh-basicinfo", method = HttpMethod.POST)
	public void refreshBasicInfoData(Context c) {
		MonitorInfoQuery query = new MonitorInfoQuery();
		query.instance = c.getString("instance", true);
		query.name = c.getString("name", true);
		query.type = c.getString("type", true);
		List<MonitorInfo> list = MonitorManager.get().getData(query);
		Map<String, String> map = new LinkedHashMap<>();
		if (!list.isEmpty()) {
			MonitorInfo info = list.get(list.size() - 1);
			map = JSONUtil.fromJson(info.value, LinkedHashMap.class);
		}
		c.put("errorCode", 0);
		c.put("info", map);
		c.view(new JsonView());
	}

	@Service(id = "refresh-chart", method = HttpMethod.POST)
	public void refreshChartData(Context c) {
		MonitorInfoQuery query = new MonitorInfoQuery();
		query.instance = c.getString("instance", true);
		query.name = c.getString("name", true);
		query.type = c.getString("type", true);
		query.startTime = c.getLong("startTime");
		query.endTime = c.getLong("endTime");
		List<MonitorInfo> list = MonitorManager.get().getData(query);
		List<Long> labels = new ArrayList<>();
		Map<String, List<Double>> datasets = new LinkedHashMap<>();
		Map<String,Double> lastCountValue=new HashMap<>();
		for (MonitorInfo e : list) {
			LinkedHashMap<String, String> values = JSONUtil.fromJson(e.value, LinkedHashMap.class);
			for (Map.Entry<String, String> entry : values.entrySet()) {
				List<Double> datas = datasets.get(entry.getKey());
				if (datas == null) {
					datas = new ArrayList<>();
					datasets.put(entry.getKey(), datas);
				}
				double value=Double.valueOf(entry.getValue());
				//如果是count类型的
				if (MonitorInfo.CATEGORY_TYPE_COUNT.equals(e.type)) {
					if(lastCountValue.containsKey(entry.getKey())){
						datas.add(value-lastCountValue.get(entry.getKey()));
					}
					lastCountValue.put(entry.getKey(),value);
				}else{
					datas.add(value);
				}
				
			}
			labels.add(e.time);
		}
		//
		
		c.put("datasets", datasets);
		c.put("labels", labels);
		c.put("errorCode", 0);
		c.view(new JsonView());
	}

}
