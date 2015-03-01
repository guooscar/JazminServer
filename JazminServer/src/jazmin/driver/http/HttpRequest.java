/**
 * 
 */
package jazmin.driver.http;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpRequest {
	BoundRequestBuilder requestBuilder;
	HttpClientDriver driver;
	public HttpRequest(HttpClientDriver driver,BoundRequestBuilder builder) {
		this.requestBuilder=builder;
		this.driver=driver;
	}
	//
	/**
	 */
	public HttpRequest addCookie(HttpCookie cookie) {
		requestBuilder.addCookie(cookie.cookie);
		return this;
	}
	/**
	 */
	public HttpRequest addFormParam(String key, String value) {
		requestBuilder.addFormParam(key, value);
		return this;
	}
	/**
	 */
	public HttpRequest addHeader(String name, String value) {
		requestBuilder.addHeader(name, value);
		return this;
		
	}
	/**
	 */
	public HttpRequest addOrReplaceCookie(HttpCookie cookie) {
		requestBuilder.addOrReplaceCookie(cookie.cookie);
		return this;
		
	}
	/**
	 */
	public HttpRequest addQueryParam(String name, String value) {
		requestBuilder.addQueryParam(name, value);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBody(byte[] data) {
		requestBuilder.setBody(data);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBody(File file) {
		requestBuilder.setBody(file);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBody(InputStream stream) {
		requestBuilder.setBody(stream);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBody(List<byte[]> data) {
		requestBuilder.setBody(data);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBody(String data) {
		requestBuilder.setBody(data);
		return this;
		
	}
	/**
	 */
	public HttpRequest setBodyEncoding(String charset) {
		requestBuilder.setBodyEncoding(charset);
		return this;
	}
	/**
	 */
	public HttpRequest setContentLength(int length) {
		requestBuilder.setContentLength(length);
		return this;
		
	}
	/**
	 */
	public HttpRequest setCookies(Collection<HttpCookie> cookies) {
		cookies.forEach(c->requestBuilder.addCookie(c.cookie));
		return this;
		
	}
	/**
	 */
	public HttpRequest setFollowRedirects(boolean followRedirects) {
		requestBuilder.setFollowRedirects(followRedirects);
		return this;
		
	}
	/**
	 */
	public HttpRequest setFormParams(Map<String, List<String>> params) {
		requestBuilder.setFormParams(params);
		return this;
		
	}
	/**
	 */
	public HttpRequest setHeader(String name, String value) {
		requestBuilder.setHeader(name, value);
		return this;
		
	}
	/**
	 */
	public HttpRequest setInetAddress(InetAddress address) {
		requestBuilder.setInetAddress(address);
		return this;
		
	}
	/**
	 */
	public HttpRequest setLocalInetAddress(InetAddress address) {
		requestBuilder.setLocalInetAddress(address);
		return this;
		
	}
	/**
	 */
	public HttpRequest setMethod(String method) {
		requestBuilder.setMethod(method);
		return this;
		
	}
	/**
	 */
	public HttpRequest setProxyServer(HttpProxyServer proxyServer) {
		requestBuilder.setProxyServer(proxyServer.proxyServer);
		return this;
		
	}
	/**
	 */
	public HttpRequest setQueryParams(Map<String, List<String>> map) {
		requestBuilder.setQueryParams(map);
		return this;
		
	}
	/**
	 */
	public HttpRequest setRangeOffset(long rangeOffset) {
		requestBuilder.setRangeOffset(rangeOffset);
		return this;
		
	}
	/**
	 */
	public HttpRequest setRequestTimeout(int requestTimeout) {
		requestBuilder.setRequestTimeout(requestTimeout);
		return this;
		
	}
	/**
	 */
	public HttpRequest setUrl(String url) {
		requestBuilder.setUrl(url);
		return this;
		
	}
	/**
	 */
	public HttpRequest setVirtualHost(String virtualHost) {
		requestBuilder.setVirtualHost(virtualHost);
		return this;
	}

	//
	public static class HttpResponseRunnable implements Runnable{
		HttpResponseHandler responseHandler;
		HttpResponse response;
		Throwable e;
		//
		public HttpResponseRunnable(HttpResponseHandler h,HttpResponse r,Throwable e) {
			this.responseHandler=h;
			this.response=r;
			this.e=e;
		}
		@Override
		public void run() {
			responseHandler.handle(response, e);
		}
	}
	//
	/**
	 */
	public void execute(HttpResponseHandler handler) {
		HttpHandler h=new HttpHandler(driver,requestBuilder.build(),handler);
		int id=driver.addHandler(h);
		h.id=id;
		requestBuilder.execute(h);
	}
	/**
	 */
	public HttpFeature execute() {
		HttpHandler h=new HttpHandler(driver,requestBuilder.build(),null);
		int id=driver.addHandler(h);
		h.id=id;
		return new HttpFeature(requestBuilder.execute(h));
	}
	/**
	 */
	public BoundRequestBuilder setUri(HttpURI uri) {
		return requestBuilder.setUri(uri.uri);
	}
}
