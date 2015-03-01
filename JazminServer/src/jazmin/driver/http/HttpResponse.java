/**
 * 
 */
package jazmin.driver.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpResponse {
	Response response;
	public HttpResponse(Response response) {
		this.response=response;
	}
	//
	/**
	 */
	public String getContentType() {
		return response.getContentType();
	}
	/**
	 */
	public List<HttpCookie> getCookies() {
		List<HttpCookie>result=new ArrayList<HttpCookie>();
		response.getCookies().forEach(c->result.add(new HttpCookie(c)));
		return result;
	}
	/**
	 */
	public String getHeader(String arg0) {
		return response.getHeader(arg0);
	}
	/**
	 */
	public Map<String,List<String>> getHeaders() {
		Map<String,List<String>>result=new HashMap<String, List<String>>();
		FluentCaseInsensitiveStringsMap map= response.getHeaders();
		map.forEach((k,v)->{
			result.put(k, v);
		});
		return result;
	}
	/**
	 */
	public List<String> getHeaders(String arg0) {
		return response.getHeaders(arg0);
	}
	/**
	 */
	public String getResponseBody() throws IOException {
		return response.getResponseBody();
	}
	/**
	 */
	public String getResponseBody(String arg0) throws IOException {
		return response.getResponseBody(arg0);
	}
	/**
	 */
	public ByteBuffer getResponseBodyAsByteBuffer() throws IOException {
		return response.getResponseBodyAsByteBuffer();
	}
	/**
	 */
	public byte[] getResponseBodyAsBytes() throws IOException {
		return response.getResponseBodyAsBytes();
	}
	/**
	 */
	public InputStream getResponseBodyAsStream() throws IOException {
		return response.getResponseBodyAsStream();
	}
	/**
	 */
	public String getResponseBodyExcerpt(int arg0, String arg1)
			throws IOException {
		return response.getResponseBodyExcerpt(arg0, arg1);
	}
	/**
	 */
	public String getResponseBodyExcerpt(int arg0) throws IOException {
		return response.getResponseBodyExcerpt(arg0);
	}
	/**
	 */
	public int getStatusCode() {
		return response.getStatusCode();
	}
	/**
	 */
	public String getStatusText() {
		return response.getStatusText();
	}
	/**
	 */
	public boolean hasResponseBody() {
		return response.hasResponseBody();
	}
	/**
	 */
	public boolean hasResponseHeaders() {
		return response.hasResponseHeaders();
	}
	/**
	 */
	public boolean hasResponseStatus() {
		return response.hasResponseStatus();
	}
	/**
	 */
	public boolean isRedirected() {
		return response.isRedirected();
	}
	
	/**
	 * @return
	 * @see com.ning.http.client.Response#getUri()
	 */
	public HttpURI getUri() {
		return new HttpURI(response.getUri());
	}
	/**
	 */
	public String toString() {
		return response.toString();
	}
}
