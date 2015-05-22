/**
 * 
 */
package jazmin.test.driver.http;

import jazmin.driver.http.HttpClientDriver;

/**
 * @author yama
 *
 */
public class NingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		HttpClientDriver d=new HttpClientDriver();
		d.init();
		d.start();
		String s=d.get("http://www.baidu.com/").execute().get().getResponseBody();
		System.out.println(s);
	}

}
