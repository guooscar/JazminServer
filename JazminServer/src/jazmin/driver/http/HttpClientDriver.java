package jazmin.driver.http;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

/**
 * 
 * @author yama
 * 11 Feb, 2015
 */
public class HttpClientDriver extends Driver{
	//
	private static final String DEFAULT_UA="Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; "
			+ "Trident/4.0; Acoo Browser 1.98.744; .NET CLR 3.5.30729)";
	//
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	//
	Map<Integer,HttpHandler>handlerMap;
	LinkedList<String>errorLogs;
	//
	public HttpClientDriver() {
		handlerMap=new ConcurrentHashMap<Integer, HttpHandler>();
		asyncHttpClient = new AsyncHttpClient();
		clientConfigBuilder=new Builder();
		errorLogs=new LinkedList<String>();
	}
	//
	@Override
	public void init() throws Exception {
		clientConfigBuilder.setUserAgent(DEFAULT_UA);
		clientConfigBuilder.setAsyncHttpClientProviderConfig(new NettyAsyncHttpProviderConfig());
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(clientConfig);
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new HttpClientDriverCommand());
		}
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
				.print("asyncHttpProvider",clientConfig.getAsyncHttpProviderConfig().getClass())
				.print("isCompressionEnforced",isCompressionEnforced())
				.print("isFollowRedirect",isFollowRedirect())
				.print("maxConnections",getMaxConnections())
				.print("maxConnectionsPerHost",getMaxConnectionsPerHost())
				.print("userAgent",getUserAgent())
				.print("pooledConnectionIdleTimeout",getPooledConnectionIdleTimeout())
				.print("readTimeout",getReadTimeout())
				.print("maxRequestRetry",getMaxRequestRetry())
				.print("connectionTTL",getConnectionTTL())
				.print("connectTimeout",getConnectTimeout())
				.toString();			
	}
	//--------------------------------------------------------------------------
	private AtomicInteger handlerIds=new AtomicInteger();
	int addHandler(HttpHandler handler){
		int id=handlerIds.incrementAndGet();
		handlerMap.put(id, handler);
		return id;
	}
	//
	void removeHandler(int id){
		handlerMap.remove(id);
	}
	//
	List<HttpHandler>getHandlers(){
		return new ArrayList<HttpHandler>(handlerMap.values());
	}
	//
	void addErrorLog(String msg){
		synchronized (errorLogs) {
			if(errorLogs.size()>500){
				errorLogs.removeFirst();
			}
			errorLogs.add(msg);
		}
	}
	//
	List<String>getErrorLogs(){
		synchronized (errorLogs) {
			return new ArrayList<String>(errorLogs);			
		}
	}
	//--------------------------------------------------------------------------
	/**
	 */
	public int getConnectTimeout() {
		return clientConfig.getConnectTimeout();
	}
	/**
	 */
	public void setCompressionEnforced(boolean compressionEnforced) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setCompressionEnforced(compressionEnforced);
	}
	/**
	 */
	public void setConnectTimeout(int connectTimeOut) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectTimeout(connectTimeOut);
	}
	/**
	 */
	public void setConnectionTTL(int connectionTTL) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectionTTL(connectionTTL);
	}

	/**
	 */
	public void setDisableUrlEncodingForBoundedRequests(
			boolean disableUrlEncodingForBoundedRequests) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setDisableUrlEncodingForBoundedRequests(disableUrlEncodingForBoundedRequests);
	}


	/**
	 */
	public void setFollowRedirect(boolean followRedirect) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setFollowRedirect(followRedirect);
	}


	/**
	 */
	public void setMaxConnections(int maxConnections) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxConnections(maxConnections);
	}


	/**
	 */
	public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setMaxConnectionsPerHost(maxConnectionsPerHost);
	}

	/**
	 */
	public void setMaxRedirects(int maxRedirects) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setMaxRedirects(maxRedirects);
	}


	/**
	 */
	public void setMaxRequestRetry(int maxRequestRetry) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxRequestRetry(maxRequestRetry);
	}


	/**
	 */
	public void setPooledConnectionIdleTimeout(
			int pooledConnectionIdleTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder
				.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
	}


	/**
	 */
	public void setReadTimeout(int readTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setReadTimeout(readTimeout);
	}


	/**
	 */
	public void setRequestTimeout(int requestTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setRequestTimeout(requestTimeout);
	}


	/**
	 */
	public void setUserAgent(String userAgent) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setUserAgent(userAgent);
	}


	/**
	 */
	public int getConnectionTTL() {
		return clientConfig.getConnectionTTL();
	}

	/**
	 */
	public int getMaxConnections() {
		return clientConfig.getMaxConnections();
	}

	/**
	 */
	public int getMaxConnectionsPerHost() {
		return clientConfig.getMaxConnectionsPerHost();
	}

	/**
	 */
	public int getMaxRequestRetry() {
		return clientConfig.getMaxRequestRetry();
	}

	/**
	 */
	public int getPooledConnectionIdleTimeout() {
		return clientConfig.getPooledConnectionIdleTimeout();
	}

	/**
	 */
	public int getReadTimeout() {
		return clientConfig.getReadTimeout();
	}

	/**
	 */
	public String getUserAgent() {
		return clientConfig.getUserAgent();
	}

	/**
	 */
	public boolean isCompressionEnforced() {
		return clientConfig.isCompressionEnforced();
	}

	/**
	 */
	public boolean isFollowRedirect() {
		return clientConfig.isFollowRedirect();
	}

	//--------------------------------------------------------------------------
	//
	public HttpRequest get(String url){
		HttpRequest req=new HttpRequest(this,asyncHttpClient.prepareGet(url));
		return req;
	}
	public HttpRequest post(String url){
		HttpRequest req=new HttpRequest(this,asyncHttpClient.preparePost(url));
		return req;
	}
	public HttpRequest head(String url){
		HttpRequest req=new HttpRequest(this,asyncHttpClient.prepareHead(url));
		return req;
	}
	public HttpRequest delete(String url){
		HttpRequest req=new HttpRequest(this,asyncHttpClient.prepareDelete(url));
		return req;
	}
	public HttpRequest options(String url){
		HttpRequest req=new HttpRequest(this,asyncHttpClient.prepareOptions(url));
		return req;
	}
}
