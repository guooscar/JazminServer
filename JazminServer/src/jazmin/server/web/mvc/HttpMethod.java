package jazmin.server.web.mvc;
/**
 * 
 * @author yama
 * 29 Dec, 2014
 */
public enum HttpMethod {
	GET("GET"), 
	POST("POST"), 
	PUT("PUT"), 
	DELETE("DELETE"),
	OPTIONS("OPTIONS"),
	TRACE("TRACE");
	
	private String httpMethod;
	
	HttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	@Override
	public String toString() {
		return httpMethod;
	}
}