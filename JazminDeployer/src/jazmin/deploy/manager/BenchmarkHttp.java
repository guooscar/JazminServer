/**
 * 
 */
package jazmin.deploy.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

/**
 * @author yama
 *
 */
public class BenchmarkHttp {
	static interface SampleAction{
		Object run()throws Exception;
	}
	//
	BenchmarkSession session;
	private static final String DEFAULT_UA="JazminDeployer HttpRobot";
	static AsyncHttpClientConfig.Builder clientConfigBuilder;
	static AsyncHttpClientConfig clientConfig;
	static AsyncHttpClient asyncHttpClient;
	static{
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setUserAgent(DEFAULT_UA);
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(clientConfig),clientConfig);		
	}
	//
	public BenchmarkHttp(BenchmarkSession session) {
		this.session=session;
	}
	//
	public static class HttpResponse{
		public String body;
		public Map<String,List<String>>headers;
		public int statusCode;
		public HttpResponse() {
			headers=new HashMap<>();
		}
	}
	//
	private Object sample(String name,SampleAction action){
		long start=System.currentTimeMillis();
		boolean error=false;
		try{
			return action.run();
		}catch (Exception e) {
			session.log(e.getMessage());
			error=true;
			return null;
		}finally {
			session.sample("[http]"+name,System.currentTimeMillis()-start, error);
		}
	}
	//
	public HttpResponse post(String url){
		return post(url,null,null,null);
	}
	//
	public HttpResponse post(String url,
			Map<String,String>headers,
			Map<String,String>parameters,
			Map<String,String> formValues){
		return (HttpResponse) sample(url,()->{
			 BoundRequestBuilder builder=asyncHttpClient.preparePost(url);
			 if(headers!=null){
				 headers.forEach((k,v)->{
					 builder.addHeader(k, v);
				 });
			 }
			 if(parameters!=null){
				 parameters.forEach((k,v)->{
					 builder.addQueryParam(k, v);
				 });
			 }
			 if(formValues!=null){
				 formValues.forEach((k,v)->{
					 builder.addFormParam(k, v);
				 });
			 }
			 session.log("[post]"+url);
			 Response response= builder.execute().get();
			 HttpResponse hr=new HttpResponse();
			 hr.statusCode=response.getStatusCode();
			 hr.body=response.getResponseBody();
			 response.getHeaders().forEach((s,l)->{
				 hr.headers.put(s, l);
			 });
			 return hr;
		});
	}
	//
	public HttpResponse get(String url){
		return get(url,null,null);
	}
	//
	public HttpResponse get(String url,
			Map<String,String>headers,
			Map<String,String>parameters){
		return (HttpResponse) sample(url,()->{
			 BoundRequestBuilder builder=asyncHttpClient.prepareGet(url);
			 if(headers!=null){
				 headers.forEach((k,v)->{
					 builder.addHeader(k, v);
				 });
			 }
			 if(parameters!=null){
				 parameters.forEach((k,v)->{
					 builder.addQueryParam(k, v);
				 });
			 }
			 session.log("[get]"+url);
			 Response response= builder.execute().get();
			 HttpResponse hr=new HttpResponse();
			 hr.statusCode=response.getStatusCode();
			 hr.body=response.getResponseBody();
			 response.getHeaders().forEach((s,l)->{
				 hr.headers.put(s, l);
			 });
			 return hr;
		});
	}
}