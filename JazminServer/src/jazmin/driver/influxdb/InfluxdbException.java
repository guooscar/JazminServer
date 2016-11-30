/**
 * 
 */
package jazmin.driver.influxdb;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class InfluxdbException extends RuntimeException{
	public InfluxdbException() {
	}
	public InfluxdbException(Throwable e){
		super(e);
	}
	public InfluxdbException(String msg){
		super(msg);
	}
}
