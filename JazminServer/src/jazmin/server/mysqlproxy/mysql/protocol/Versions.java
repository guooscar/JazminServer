package jazmin.server.mysqlproxy.mysql.protocol;

/**
 * 
 * <pre><b>proxy's version.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 */
public interface Versions {

	byte PROTOCOL_VERSION = 10;
	byte[] SERVER_VERSION = "m2o-proxy-5.6.0-snapshot".getBytes();
}