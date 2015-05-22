/**
 * 
 */
package jazmin.test.driver.redis;

import jazmin.core.Jazmin;
import jazmin.driver.redis.RedisConnection;
import jazmin.driver.redis.RedisDriver;
import jazmin.server.console.ConsoleServer;

/**
 * @author g2131
 *
 */
public class RedisDriverTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		RedisDriver d=new RedisDriver();
		d.addShard("10.44.218.35", 6379);
		Jazmin.addDriver(d);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
		//
		RedisConnection conn=d.getConnection();
		conn.set("test","test");
		String v=conn.get("test");
		System.out.println(v);
	}

}
