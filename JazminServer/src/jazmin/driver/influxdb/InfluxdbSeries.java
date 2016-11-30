/**
 * 
 */
package jazmin.driver.influxdb;

import java.util.List;

/**
 * @author yama
 *
 */
public class InfluxdbSeries {
	public String name;
	public String columns[];
	public List<String[]>values;
	public InfluxdbSeries() {
		columns=new String[0];
	}
}
