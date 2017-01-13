/**
 * 
 */
package jazmin.driver.influxdb;

/**
 * @author yama
 *
 */
public class InfluxdbResult {
	public InfluxdbSeries series[];
	public String error;
	public InfluxdbResult() {
		series=new InfluxdbSeries[0];
	}
}