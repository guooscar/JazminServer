package jazmin.server.web.mvc;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * Provides functionality for modifying the response
 *
 * @author Per Wendel
 */
public class Response {
	public static final String DEFAULT_CHARSET_ENCODING="UTF-8";
	/**
	 * The logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Response.class);
	private HttpServletResponse response;
	private String body;
	//
	protected Response() {
		// Used by wrapper
	}

	Response(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * Sets the status code for the
	 *
	 * @param statusCode
	 *            the status code
	 */
	public void status(int statusCode) {
		response.setStatus(statusCode);
	}

	/**
	 * Sets the content type for the response
	 *
	 * @param contentType
	 *            the content type
	 */
	public void type(String contentType) {
		response.setContentType(contentType);
	}

	/**
	 * Sets the body
	 *
	 * @param body
	 *            the body
	 */
	public void body(String body) {
		this.body = body;
	}

	/**
	 * returns the body
	 *
	 * @return the body
	 */
	public String body() {
		return this.body;
	}

	/**
	 * @return the raw response object handed in by Jetty
	 */
	public HttpServletResponse raw() {
		return response;
	}

	/**
	 * Trigger a browser redirect
	 *
	 * @param location
	 *            Where to redirect
	 */
	public void redirect(String location) {
		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting  {}", location);
		}
		try {
			response.sendRedirect(location);
		} catch (IOException ioException) {
			logger.warn("Redirect failure", ioException);
		}
	}

	/**
	 * Trigger a browser redirect with specific http 3XX status code.
	 *
	 * @param location
	 *            Where to redirect permanently
	 * @param httpStatusCode
	 *            the http status code
	 */
	public void redirect(String location, int httpStatusCode) {
		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting {} to {}", httpStatusCode, location);
		}
		response.setStatus(httpStatusCode);
		response.setHeader("Location", location);
		response.setHeader("Connection", "close");
		try {
			response.sendError(httpStatusCode);
		} catch (IOException e) {
			logger.warn("Exception when trying to redirect permanently", e);
		}
	}

	/**
	 * Adds/Sets a response header
	 *
	 * @param header
	 *            the header
	 * @param value
	 *            the value
	 */
	public void header(String header, String value) {
		response.addHeader(header, value);
	}

	/**
	 * Adds not persistent cookie to the response. Can be invoked multiple times
	 * to insert more than one cookie.
	 *
	 * @param name
	 *            name of the cookie
	 * @param value
	 *            value of the cookie
	 */
	public void cookie(String name, String value) {
		cookie(name, value, -1, false);
	}

	/**
	 * Adds cookie to the response. Can be invoked multiple times to insert more
	 * than one cookie.
	 *
	 * @param name
	 *            name of the cookie
	 * @param value
	 *            value of the cookie
	 * @param maxAge
	 *            max age of the cookie in seconds (negative for the not
	 *            persistent cookie, zero - deletes the cookie)
	 */
	public void cookie(String name, String value, int maxAge) {
		cookie(name, value, maxAge, false);
	}

	/**
	 * Adds cookie to the response. Can be invoked multiple times to insert more
	 * than one cookie.
	 *
	 * @param name
	 *            name of the cookie
	 * @param value
	 *            value of the cookie
	 * @param maxAge
	 *            max age of the cookie in seconds (negative for the not
	 *            persistent cookie, zero - deletes the cookie)
	 * @param secured
	 *            if true : cookie will be secured zero - deletes the cookie)
	 */
	public void cookie(String name, String value, int maxAge, boolean secured) {
		cookie("", name, value, maxAge, secured);
	}

	/**
	 * Adds cookie to the response. Can be invoked multiple times to insert more
	 * than one cookie.
	 *
	 * @param path
	 *            path of the cookie
	 * @param name
	 *            name of the cookie
	 * @param value
	 *            value of the cookie
	 * @param maxAge
	 *            max age of the cookie in seconds (negative for the not
	 *            persistent cookie, zero - deletes the cookie)
	 * @param secured
	 *            if true : cookie will be secured zero - deletes the cookie)
	 */
	public void cookie(String path, String name, String value, int maxAge,
			boolean secured) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setSecure(secured);
		response.addCookie(cookie);
	}

	/**
	 * Removes the cookie.
	 *
	 * @param name
	 *            name of the cookie
	 */
	public void removeCookie(String name) {
		Cookie cookie = new Cookie(name, "");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
