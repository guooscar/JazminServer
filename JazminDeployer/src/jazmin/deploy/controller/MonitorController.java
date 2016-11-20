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

import jazmin.deploy.domain.monitor.KeyValue;
import jazmin.deploy.domain.monitor.MonitorChartData;
import jazmin.deploy.domain.monitor.MonitorInfo;
import jazmin.deploy.domain.monitor.MonitorInfoQuery;
import jazmin.deploy.manager.MonitorManager;
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
		String machine=c.request().raw().getRemoteAddr();
		info.machine=machine;
		info.instance = c.getString("instance");
		info.time = c.getLong("time");
		info.type = c.getString("type");
		info.name = c.getString("name");
		info.value = c.request().body();
		MonitorManager.get().addMonitorInfo(info);
		c.view(new JsonView());
	}

	private static final Map<String, KeyValue<Long, String>> FORMAT = new HashMap<>();
	static {
		FORMAT.put("FileServer.HomeDirSize", new KeyValue<>(1000000L, "MB"));
		FORMAT.put("FileDriver.HomeDirSize", new KeyValue<>(1000000L, "MB"));
		FORMAT.put("VM.MemoryHeap", new KeyValue<>(1000000L, "MB"));
		FORMAT.put("VM.MemoryNonHeap", new KeyValue<>(1000000L, "MB"));
		FORMAT.put("RpcServer.Network", new KeyValue<>(1000L, "KB"));
		FORMAT.put("Count", new KeyValue<>(1L, "per 10s"));
	}

	/**
	 * description
	 * 
	 * @param c
	 */
	@Service(id = "view")
	public void monitorView(Context c) {
		String instances = c.getString("instances", true);
		String keyvalues = c.getStringOrDefault("keyvalues", "");
		String charts = c.getStringOrDefault("charts", "");
		Set<String> instanceSet = new TreeSet<>();
		Set<String> inclueSet = new TreeSet<>();
		String[] is = instances.split("\\$");
		String[] kvs = keyvalues.split("\\$");
		String[] cs = charts.split("\\$");
		for (String temp : is) {
			instanceSet.add(temp);
		}
		for (String temp : kvs) {
			inclueSet.add(temp);
		}
		for (String temp : cs) {
			inclueSet.add(temp);
		}
		List<MonitorInfo> list = new ArrayList<>();
		for (String temp : instanceSet) {
			list.addAll(monitor(inclueSet, temp));
		}
		list.sort((a, b) -> {
			return a.name.compareTo(b.name);
		});
		c.put("list", list);
		c.view(new ResourceView("/jsp/monitor.jsp"));
	}

	/**
	 * 获取监控数据
	 * 
	 * @param inclueSet
	 * @param instance
	 * @return
	 */
	private List<MonitorInfo> monitor(Set<String> inclueSet, String instance) {
		List<MonitorInfo> list = MonitorManager.get().getMonitorInfos(instance);
		Iterator<MonitorInfo> iterator = list.iterator();
		while (iterator.hasNext()) {
			MonitorInfo info = iterator.next();
			if (inclueSet.contains(info.name)) {
				KeyValue<Long, String> kv = FORMAT.get(info.name);
				if (kv != null) {
					info.description = kv.getValue();
				} else {
					kv = FORMAT.get(info.type);
				}
				if (kv != null) {
					info.description = kv.getValue();
				}
				continue;
			}
			iterator.remove();
		}
		return list;
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

	@Service(id = "refresh-charts", method = HttpMethod.POST)
	public void refreshChartDatas(Context context) {
		String charts = context.getString("charts", true);
		Long startTime = context.getLong("startTime");
		Long endTime = context.getLong("endTime");
		String[] chartArray = charts.split("\\$");
		List<MonitorChartData> datas = new ArrayList<>();
		for (String chart : chartArray) {
			MonitorChartData data = new MonitorChartData();
			String[] items = chart.split("\\:");
			if (items.length != 4) {
				continue;
			}
			data.chartId = items[0];
			data.instance = items[1];
			data.name = items[2];
			data.type = items[3];
			data = refreshChartData(startTime, endTime, data);
			datas.add(data);
		}
		context.put("datas", datas);
		context.put("errorCode", 0);
		context.view(new JsonView());
	}

	private MonitorChartData refreshChartData(Long startTime, Long endTime, MonitorChartData data) {
		MonitorInfoQuery query = new MonitorInfoQuery();
		query.instance = data.instance;
		query.name = data.name;
		query.type = data.type;
		query.startTime = startTime;
		query.endTime = endTime;
		KeyValue<Long, String> kv = FORMAT.get(data.name);
		Long rate = 1L;
		if (kv != null) {
			rate = kv.getKey();
		}
		List<MonitorInfo> list = MonitorManager.get().getData(query);
		List<Long> labels = new ArrayList<>();
		Map<String, List<Double>> datasets = new LinkedHashMap<>();
		Map<String, Double> lastCountValue = new HashMap<>();
		for (MonitorInfo e : list) {
			LinkedHashMap<String, String> values = JSONUtil.fromJson(e.value, LinkedHashMap.class);
			for (Map.Entry<String, String> entry : values.entrySet()) {
				List<Double> datas = datasets.get(entry.getKey());
				if (datas == null) {
					datas = new ArrayList<>();
					datasets.put(entry.getKey(), datas);
				}
				double value = Double.valueOf(entry.getValue());
				value = value / rate;
				// 如果是count类型的
				if (MonitorInfo.CATEGORY_TYPE_COUNT.equals(e.type)) {
					if (lastCountValue.containsKey(entry.getKey())) {
						double v = value - lastCountValue.get(entry.getKey());
						if (v < 0) {
							v = 0;
						}
						datas.add(v);
					}
					lastCountValue.put(entry.getKey(), value);
				} else {
					datas.add(value);
				}
			}
			labels.add(e.time);
		}
		data.datasets = datasets;
		data.labels = labels;
		return data;
	}

}