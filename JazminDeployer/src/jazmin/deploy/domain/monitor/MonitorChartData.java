package jazmin.deploy.domain.monitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author ginko.wang
 *
 */
public class MonitorChartData {

	public String chartId;
	public String name;
	public String type;
	public List<Long> labels;
	public Map<String, List<Double>> datasets;

	public MonitorChartData() {
		this.labels = new ArrayList<>();
		this.datasets = new LinkedHashMap<>();
	}
}
