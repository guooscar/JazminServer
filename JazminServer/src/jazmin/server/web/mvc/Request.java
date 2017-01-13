package jazmin.server.web.mvc;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.IOUtil;

/**
 * 
 * @author yama
 * 29 Dec, 2014
 */
public class Request {
	private static Logger logger =LoggerFactory.get(Request.class);
	private static final String USER_AGENT = "user-agent";
	private List<String> querys;
	private HttpServletRequest servletRequest;
	private HttpSession session = null;
	/* Lazy loaded stuff */
	private String body = null;
	private byte[] bodyAsBytes = null;
	private Set<String> headers = null;

	// request.body # request body sent by the client (see below), DONE
	// request.scheme # "http" DONE
	// request.path_info # "/foo", DONE
	// request.port # 80 DONE
	// request.request_method # "GET", DONE
	// request.query_string # "", DONE
	// request.content_length # length of request.body, DONE
	// request.media_type # media type of request.body DONE, content type?
	// request.host # "example.com" DONE
	// request["SOME_HEADER"] # value of SOME_HEADER header, DONE
	// request.user_agent # user agent (used by :agent condition) DONE
	// request.url # "http://example.com/example/foo" DONE
	// request.ip # client IP address DONE
	// request.env # raw env hash handed in by Rack, DONE
	// request.get? # true (similar methods for other verbs)
	// request.secure? # false (would be true over ssl)
	// request.forwarded? # true (if running behind a reverse proxy)
	// request.cookies # hash of browser cookies, DONE
	// request.xhr? # is this an ajax request?
	// request.script_name # "/example"
	// request.form_data? # false
	// request.referrer # the referrer of the client or '/'
	/**
	*/
	Request(HttpServletRequest request) {
		this.servletRequest=request;
		String ss[]=request.getRequestURI().split("/");
		querys=new ArrayList<String>();
		for(int i=2;i<ss.length;i++){
			querys.add(ss[i]);
		}
	}
	//
	public List<String>querys(){
		return querys;
	}
	//
	/**
	 * @return request method e.g. GET, POST, PUT, ...
	 */
	public String requestMethod() {
		return servletRequest.getMethod();
	}

	/**
	 * @return the scheme
	 */
	public String scheme() {
		return servletRequest.getScheme();
	}

	/**
	 * @return the host
	 */
	public String host() {
		return servletRequest.getHeader("host");
	}

	/**
	 * @return the user-agent
	 */
	public String userAgent() {
		return servletRequest.getHeader(USER_AGENT);
	}

	/**
	 * @return the server port
	 */
	public int port() {
		return servletRequest.getServerPort();
	}

	/**
	 * @return the path info Example return: "/example/foo"
	 */
	public String pathInfo() {
		return servletRequest.getPathInfo();
	}

	/**
	 * @return the servlet path
	 */
	public String servletPath() {
		return servletRequest.getServletPath();
	}

	/**
	 * @return the context path
	 */
	public String contextPath() {
		return servletRequest.getContextPath();
	}

	/**
	 * @return the URL string
	 */
	public String url() {
		return servletRequest.getRequestURL().toString();
	}

	/**
	 * @return the content type of the body
	 */
	public String contentType() {
		return servletRequest.getContentType();
	}
	
	/**
	 * @return the client's IP address
	 */
	public String ip() {
		String ret = null;
		ret = servletRequest.getHeader("X-Forwarded-For");
		if (ret == null || ret.trim().isEmpty()) {
			ret = servletRequest.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ret == null || ret.trim().isEmpty()) {
			ret = servletRequest.getRemoteAddr();
		}
		return ret;
	}

	/**
	 * @return the request body sent by the client
	 */
	public String body() {
		if (body == null) {
			readBody();
		}
		return body;
	}

	public byte[] bodyAsBytes() {
		if (bodyAsBytes == null) {
			readBody();
		}
		return bodyAsBytes;
	}

	private void readBody() {
		try {
			bodyAsBytes = IOUtil.toByteArray(servletRequest.getInputStream());
			body = IOUtil.toString(new ByteArrayInputStream(bodyAsBytes));
		} catch (Exception e) {
			logger.warn("Exception when reading body", e);
		}
	}

	/**
	 * @return the length of request.body
	 */
	public int contentLength() {
		return servletRequest.getContentLength();
	}

	/**
	 * gets the query param
	 *
	 * @param queryParam
	 *            the query parameter
	 * @return the value of the provided queryParam Example: query parameter
	 *         'id' from the following request URI: /hello?id=foo
	 */
	public String queryParams(String queryParam) {
		return servletRequest.getParameter(queryParam);
	}

	/**
	 * Gets the value for the provided header
	 *
	 * @param header
	 *            the header
	 * @return the value of the provided header
	 */
	public String headers(String header) {
		return servletRequest.getHeader(header);
	}

	/**
	 * @return all query parameters
	 */
	public Set<String> queryParams() {
		return servletRequest.getParameterMap().keySet();
	}

	/**
	 * @return all headers
	 */
	public Set<String> headers() {
		if (headers == null) {
			headers = new TreeSet<String>();
			Enumeration<String> enumeration = servletRequest.getHeaderNames();
			while (enumeration.hasMoreElements()) {
				headers.add(enumeration.nextElement());
			}
		}
		return headers;
	}
	//
	public String queryURI(){
		return servletRequest.getRequestURI();
	}
	//
	/**
	 * @return the query string
	 */
	public String queryString() {
		return servletRequest.getQueryString();
	}

	/**
	 * Sets an attribute on the request (can be fetched in filters/routes later
	 * in the chain)
	 *
	 * @param attribute
	 *            The attribute
	 * @param value
	 *            The attribute value
	 */
	public void attribute(String attribute, Object value) {
		servletRequest.setAttribute(attribute, value);
	}

	/**
	 * Gets the value of the provided attribute
	 *
	 * @param attribute
	 *            The attribute value or null if not present
	 * @return the value for the provided attribute
	 */
	public Object attribute(String attribute) {
		return servletRequest.getAttribute(attribute);
	}

	/**
	 * @return all attributes
	 */
	public Set<String> attributes() {
		Set<String> attrList = new HashSet<String>();
		Enumeration<String> attributes = (Enumeration<String>) servletRequest
				.getAttributeNames();
		while (attributes.hasMoreElements()) {
			attrList.add(attributes.nextElement());
		}
		return attrList;
	}

	/**
	 * @return the raw HttpServletRequest object handed in by Jetty
	 */
	public HttpServletRequest raw() {
		return servletRequest;
	}



	/**
	 * Returns the current session associated with this request, or if the
	 * request does not have a session, creates one.
	 * @return the session associated with this request
	 */
	public HttpSession session() {
		if (session == null) {
			session=servletRequest.getSession();
		}
		return session;
	}

	/**
	 * Returns the current session associated with this request, or if there is
	 * no current session and <code>create</code> is true, returns a new
	 * session.
	 *
	 * @param create
	 *            <code>true</code> to create a new session for this request if
	 *            necessary; <code>false</code> to return null if there's no
	 *            current session
	 * @return the session associated with this request or <code>null</code> if
	 *         <code>create</code> is <code>false</code> and the request has no
	 *         valid session
	 */
	public HttpSession session(boolean create) {
		if (session == null) {
			session = servletRequest.getSession(create);
		}
		return session;
	}

	/**
	 * @return request cookies (or empty Map if cookies dosn't present)
	 */
	public Map<String, String> cookies() {
		Map<String, String> result = new HashMap<String, String>();
		Cookie[] cookies = servletRequest.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				result.put(cookie.getName(), cookie.getValue());
			}
		}
		return result;
	}

	/**
	 * Gets cookie by name.
	 *
	 * @param name
	 *            name of the cookie
	 * @return cookie value or null if the cookie was not found
	 */
	public String cookie(String name) {
		Cookie[] cookies = servletRequest.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * @return the part of this request's URL from the protocol name up to the
	 *         query string in the first line of the HTTP request.
	 */
	public String uri() {
		return servletRequest.getRequestURI();
	}

	/**
	 * @return Returns the name and version of the protocol the request uses
	 */
	public String protocol() {
		return servletRequest.getProtocol();
	}
	//
	//
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("uri:").append(uri()+"\n");
		servletRequest.getParameterMap().forEach((s,vv)->{
			sb.append(s).append(Arrays.toString(vv)+"\n");
		});
		return sb.toString();
	}
}
