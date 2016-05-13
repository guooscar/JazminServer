/**
 * 
 */
package jazmin.driver.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * @author yama
 * 13 May, 2016
 */
public class AllowAllHostnameVerifier implements HostnameVerifier{

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

}
