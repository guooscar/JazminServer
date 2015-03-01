package jazmin.driver.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.driver.http.HttpRequest.HttpResponseRunnable;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.listener.TransferCompletionHandler;
import com.ning.http.client.listener.TransferListener;

/**
 * 
 * @author yama 11 Feb, 2015
 */
public class HttpHandler extends TransferCompletionHandler implements TransferListener{
	private static Logger logger = LoggerFactory.get(HttpHandler.class);
	//
	HttpResponseHandler responseHandler;
	HttpClientDriver driver;
	Request request;
	String status;
	long startTime;
	long endTime;
	long received;
	long contentLength;
	//
	long lastReceived;
	//
	long sent;
	int id;
	Method responseMethod = HttpResponseRunnable.class.getMethods()[0];
	//
	public HttpHandler(HttpClientDriver driver,Request req, HttpResponseHandler responseHandler) {
		this.request = req;
		this.driver=driver;
		startTime = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug(responseHandler == null ? ">>>sync" : ">>>async "
					+ request.getMethod() + " " + request.getUrl());
		}
		this.responseHandler = responseHandler;
		addTransferListener(this);
	}

	@Override
	public Response onCompleted(Response response) throws Exception {
		endTime = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug(responseHandler == null ? "<<<sync" : "<<<async "
					+ request.getMethod() + " " + request.getUrl() + " ret:"
					+ (endTime - startTime));
		}
		if (responseHandler != null) {
			Jazmin.dispatcher.invokeInPool(response.getUri().getHost(),
					new HttpResponseRunnable(responseHandler, new HttpResponse(
							response), null), responseMethod);
		}
		driver.removeHandler(id);
		return response;
	}
	public AsyncHandler.STATE onContentWriteProgress(long amount, long current, long total){
		return super.onContentWriteProgress(amount, current, total);
	}
	@Override
	public void onThrowable(Throwable t) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		driver.addErrorLog(sdf.format(new Date())+"\t"+request.getUrl()+"\t"+t.getClass().getName()+"\t"+t.getMessage());
		if (logger.isDebugEnabled()) {
			logger.warn(responseHandler == null ? "<<<sync" : "<<<async "
					+ request.getMethod() + " " + request.getUrl() + "/"
					+ t.getClass());
		}
		if (responseHandler != null) {
			Jazmin.dispatcher.invokeInPool("", new HttpResponseRunnable(
					responseHandler, null, t), responseMethod);
		}
		driver.removeHandler(id);
	}
	//
	//
	@Override
	public AsyncHandler.STATE onStatusReceived (HttpResponseStatus s) throws Exception{
		this.status=s.getStatusText();
		return super.onStatusReceived(s);
	}
	//--------------------------------------------------------------------------
	@Override
	public void onBytesReceived(byte[] arg0) throws IOException {
		received+=arg0.length;
	}

	@Override
	public void onBytesSent(long amount, long current, long total) {
		sent+=current;
	}

	@Override
	public void onRequestHeadersSent(FluentCaseInsensitiveStringsMap arg0) {
		
	}

	@Override
	public void onRequestResponseCompleted() {
		
	}

	@Override
	public void onResponseHeadersReceived(FluentCaseInsensitiveStringsMap map) {
		List<String>cl=map.get("Content-Length");
		if(!cl.isEmpty()){
			try{
				this.contentLength=Long.valueOf(cl.get(0));
			}catch(Exception e){
				logger.catching(e);
			}
		}
	}
}