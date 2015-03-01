/**
 * 
 */
package jazmin.test.driver.http;

import jazmin.core.Jazmin;
import jazmin.driver.http.HttpClientDriver;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpDriverTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpClientDriver hd=new HttpClientDriver();
		Jazmin.addDriver(hd);
		ConsoleServer cs=new ConsoleServer();
		cs.port(2222);
		Jazmin.addServer(cs);
		Jazmin.start();
		//
		for(int i=0;i<10;i++){
			//hd.get("http://www.163.com").execute((rsp,e)->{
				
			hd.post("http://vaadin.com/download/release/7.3/7.3.8/vaadin-all-7.3.8.zip").execute((rsp,e)->{
				System.out.println("done");
			});
		}
	}

}
