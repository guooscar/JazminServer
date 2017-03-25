/**
 * 
 */
package jazmin.deploy.manager;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

/**
 * 
 * @author icecooly
 *
 */
public class BenchmarkRpc {
	static interface SampleAction{
		Object run()throws Exception;
	}
	//
	BenchmarkSession session;
	private static final String DEFAULT_UA="JazminDeployer RpcRobot";
	static AsyncHttpClientConfig.Builder clientConfigBuilder=new Builder();
	static AsyncHttpClientConfig clientConfig=clientConfigBuilder.build();
	static AsyncHttpClient asyncHttpClient=new AsyncHttpClient(new NettyAsyncHttpProvider(clientConfig),clientConfig);
	//
	public String url;
	public String token;
	//
	public BenchmarkRpc(BenchmarkSession session) {
		this.session=session;
		clientConfigBuilder.setUserAgent(DEFAULT_UA);
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
			session.sample(name,System.currentTimeMillis()-start, error);
		}
	}
	//
	public String invoke(
			String func,
			String ... args){
		return (String) sample(url,()->{
			 BoundRequestBuilder builder=asyncHttpClient.preparePost(url);
			 builder.addFormParam("Func", func);
			 builder.addFormParam("Token", token);
			 if(args!=null){
				 for(int i=0;i<args.length;i++){
					 builder.addQueryParam("Arg"+i, args[i]);
				 }
			 }
			 return builder.execute().get().getResponseBody();
		});
	}
}