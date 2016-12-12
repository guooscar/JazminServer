/**
 * 
 */
package jazmin.test.driver.influxdb;

import jazmin.core.Jazmin;
import jazmin.driver.influxdb.InfluxdbDriver;
import jazmin.driver.influxdb.InfluxdbResultFormatter;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class InfluxdbTest {
	public static void main(String[] args) {
		InfluxdbDriver hd=new InfluxdbDriver();
		hd.setHost("uat2.itit.io");
		hd.setPort(8086);
		hd.setDatabase("firstdb");
		hd.setUser("admin");
		hd.setPassword("itit2016");
		Jazmin.addDriver(hd);
		ConsoleServer cs=new ConsoleServer();
		Jazmin.addServer(cs);
		Jazmin.start();
		//
		//
		//System.err.println(InfluxdbResultFormatter.dump(hd.showDiagnostics()));	
		//
		//System.err.println(InfluxdbResultFormatter.dump(hd.showStats()));	
		//
		//System.err.println(InfluxdbResultFormatter.dump(hd.query("SHOW TAG KEYS FROM \"measurement_name\"")));
		//
		StringBuilder cmd=new StringBuilder();
		for(int i=0;i<50000;i++){
		//	cmd.append("cpu_load_short,host=server"+i+",region=us-west1 value=0.64 1434055562000002222"+"\n");
		}
		//hd.write(cmd.toString());
		//
		System.err.println(InfluxdbResultFormatter.dump(hd.query("select * from cpu_load_short")));
		
		//
	}
}
