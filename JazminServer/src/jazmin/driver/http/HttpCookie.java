/**
 * 
 */
package jazmin.driver.http;

import com.ning.http.client.cookie.Cookie;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpCookie {
	Cookie cookie;
	HttpCookie(Cookie c) {
		this.cookie=c;
	}
	public HttpCookie(String name,
			String value, 
			boolean rawValue,
			String domain,
			String path, 
			int maxAge,
			boolean secure,
			boolean httpOnly) {
		super();
		this.cookie=new Cookie(name, value, rawValue, domain, path,  maxAge, secure, httpOnly);
	}
	//
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#getDomain()
	 */
	public String getDomain() {
		return cookie.getDomain();
	}

	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#getMaxAge()
	 */
	public long getMaxAge() {
		return cookie.getMaxAge();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#getName()
	 */
	public String getName() {
		return cookie.getName();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#getPath()
	 */
	public String getPath() {
		return cookie.getPath();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#getRawValue()
	 */
	public String getValue() {
		return cookie.getValue();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#isHttpOnly()
	 */
	public boolean isHttpOnly() {
		return cookie.isHttpOnly();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#isSecure()
	 */
	public boolean isSecure() {
		return cookie.isSecure();
	}
	/**
	 * @return
	 * @see com.ning.http.client.cookie.Cookie#toString()
	 */
	public String toString() {
		return cookie.toString();
	}
	
}
