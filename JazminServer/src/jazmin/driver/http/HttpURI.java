/**
 * 
 */
package jazmin.driver.http;

import java.net.URI;
import java.net.URISyntaxException;

import com.ning.http.client.uri.Uri;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpURI {
	Uri uri;
	HttpURI(Uri uri) {
		this.uri=uri;
	}
	public HttpURI(String scheme, String userInfo,String host, int port, String path, String query){
		uri=new Uri(scheme, userInfo, host, port, path, query);
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getHost()
	 */
	public String getHost() {
		return uri.getHost();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getPath()
	 */
	public String getPath() {
		return uri.getPath();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getPort()
	 */
	public int getPort() {
		return uri.getPort();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getQuery()
	 */
	public String getQuery() {
		return uri.getQuery();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getScheme()
	 */
	public String getScheme() {
		return uri.getScheme();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#getUserInfo()
	 */
	public String getUserInfo() {
		return uri.getUserInfo();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#hashCode()
	 */
	public int hashCode() {
		return uri.hashCode();
	}
	/**
	 * @return
	 * @throws URISyntaxException
	 * @see com.ning.http.client.uri.Uri#toJavaNetURI()
	 */
	public URI toJavaNetURI() throws URISyntaxException {
		return uri.toJavaNetURI();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#toRelativeUrl()
	 */
	public String toRelativeUrl() {
		return uri.toRelativeUrl();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#toString()
	 */
	public String toString() {
		return uri.toString();
	}
	/**
	 * @return
	 * @see com.ning.http.client.uri.Uri#toUrl()
	 */
	public String toUrl() {
		return uri.toUrl();
	}
	/**
	 * @param newQuery
	 * @return
	 * @see com.ning.http.client.uri.Uri#withNewQuery(java.lang.String)
	 */
	public Uri withNewQuery(String newQuery) {
		return uri.withNewQuery(newQuery);
	}
	/**
	 * @param newScheme
	 * @return
	 * @see com.ning.http.client.uri.Uri#withNewScheme(java.lang.String)
	 */
	public Uri withNewScheme(String newScheme) {
		return uri.withNewScheme(newScheme);
	}
	
}
