/**
 * 
 */
package jazmin.driver.http;

/**
 * @author yama
 * 11 Feb, 2015
 */
@FunctionalInterface
public interface HttpResponseHandler {
	void handle(HttpResponse response,Throwable execption);
}
