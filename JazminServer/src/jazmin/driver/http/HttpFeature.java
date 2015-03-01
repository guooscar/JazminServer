/**
 * 
 */
package jazmin.driver.http;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpFeature {
	ListenableFuture<Response>responseFuture;
	HttpFeature(ListenableFuture<Response>responseFuture) {
		this.responseFuture=responseFuture;
	}
	//
	/**
	 */
	public void abort(Throwable arg0) {
		responseFuture.abort(arg0);
	}
	/**
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		return responseFuture.cancel(mayInterruptIfRunning);
	}
	/**
	 */
	public void done() {
		responseFuture.done();
	}
	/**
	 */
	public boolean isCancelled() {
		return responseFuture.isCancelled();
	}
	/**
	 */
	public boolean isDone() {
		return responseFuture.isDone();
	}
	/**
	 */
	public HttpResponse get() throws InterruptedException, ExecutionException {
		return new  HttpResponse(responseFuture.get());
	}
	/**
	 */
	public HttpResponse get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return new  HttpResponse(responseFuture.get(timeout, unit));
	}
	/**
	 */
	public void touch() {
		responseFuture.touch();
	}
	
}
