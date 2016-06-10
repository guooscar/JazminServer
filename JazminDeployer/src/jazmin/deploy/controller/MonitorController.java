/**
 * 
 */
package jazmin.deploy.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.SimpleDateFormat;

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
		String instance = c.getString("instance");
		List<MonitorInfo> list = MonitorManager.get().getMonitorInfos(instance);
		c.put("list", list);
		c.view(new ResourceView("/jsp/monitor.jsp"));
	}

	@Service(id = "interval", method = HttpMethod.POST)
	public void intervalData(Context c) {
		MonitorInfoQuery query = new MonitorInfoQuery();
		query.instance = c.getString("instance", true);
		query.name = c.getString("name", true);
		query.type = c.getString("type", true);
		query.startTime = c.getLongOrDefault("startTime", null);
		query.endTime = c.getLongOrDefault("startTime", null);
		List<MonitorInfo> list = MonitorManager.get().getData(query);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		List<String> labels = new ArrayList<>();
		Map<String, List<Double>> datasets = new LinkedHashMap<>();
		for (MonitorInfo e : list) {
			LinkedHashMap<String, String> values = JSONUtil.fromJson(e.value, LinkedHashMap.class);
			for (Map.Entry<String, String> entry : values.entrySet()) {
				List<Double> datas = datasets.get(entry.getKey());
				if (datas == null) {
					datas = new ArrayList<>();
					datasets.put(entry.getKey(), datas);
				}
				datas.add(Double.valueOf(entry.getValue()));
			}
			labels.add(sdf.format(new Date(e.time)));
		}
		c.put("datasets", datasets);
		c.put("labels", labels);
		c.put("errorCode", 0);
		c.view(new JsonView());
	}

}
