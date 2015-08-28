/**
 * 
 */
package jazmin.deploy.domain;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * @author yama
 *
 */
public class MemcachedUtil {

	public static String get(String host,int port,String key)throws Exception{
		XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(host+":"+port));
	    MemcachedClient client=builder.build();
	    try{
	    	return client.get(key);
	    }finally{
	       client.shutdown();
	    }
	}
	public static void add(String host,int port,String key,String value)throws Exception{
		XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(host+":"+port));
	    MemcachedClient client=builder.build();
	    try{
	    	client.add(key,0, value);
	    }finally{
	       client.shutdown();
	    }
	}
	public static void delete(String host,int port,String key)throws Exception{
		XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(host+":"+port));
	    MemcachedClient client=builder.build();
	    try{
	    	client.delete(key);
	    }finally{
	       client.shutdown();
	    }
	}
	public static void replace(String host,int port,String key,String value)throws Exception{
		XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(host+":"+port));
	    MemcachedClient client=builder.build();
	    try{
	    	client.replace(key, 0, value);
	    }finally{
	       client.shutdown();
	    }
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		MemcachedUtil.add("192.168.0.12",11211,"a","b");
		MemcachedUtil.get("192.168.0.12",11211,"a");
	}

}
