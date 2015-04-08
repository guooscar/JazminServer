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
		ConsoleServer cs=Jazmin.server(ConsoleServer.class);
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
				.print("maxConnections",maxConnections())
				.print("maxConnectionsPerHost",maxConnectionsPerHost())
				.print("userAgent",userAgent())
				.print("pooledConnectionIdleTimeout",pooledConnectionIdleTimeout())
				.print("readTimeout",readTimeout())
				.print("maxRequestRetry",maxRequestRetry())
				.print("connectionTTL",connectionTTL())
				.print("connectTimeout",connectTimeout())
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
	List<HttpHandler>handlers(){
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
	public int connectTimeout() {
		return clientConfig.getConnectTimeout();
	}
	/**
	 */
	public void compressionEnforced(boolean compressionEnforced) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setCompressionEnforced(compressionEnforced);
	}
	/**
	 */
	public void connectTimeout(int connectTimeOut) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectTimeout(connectTimeOut);
	}
	/**
	 */
	public void connectionTTL(int connectionTTL) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectionTTL(connectionTTL);
	}

	/**
	 */
	public void disableUrlEncodingForBoundedRequests(
			boolean disableUrlEncodingForBoundedRequests) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setDisableUrlEncodingForBoundedRequests(disableUrlEncodingForBoundedRequests);
	}


	/**
	 */
	public void followRedirect(boolean followRedirect) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setFollowRedirect(followRedirect);
	}


	/**
	 */
	public void maxConnections(int maxConnections) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxConnections(maxConnections);
	}


	/**
	 */
	public void maxConnectionsPerHost(int maxConnectionsPerHost) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setMaxConnectionsPerHost(maxConnectionsPerHost);
	}

	/**
	 */
	public void maxRedirects(int maxRedirects) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setMaxRedirects(maxRedirects);
	}


	/**
	 */
	public void maxRequestRetry(int maxRequestRetry) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxRequestRetry(maxRequestRetry);
	}


	/**
	 */
	public void pooledConnectionIdleTimeout(
			int pooledConnectionIdleTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder
				.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
	}


	/**
	 */
	public void readTimeout(int readTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setReadTimeout(readTimeout);
	}


	/**
	 */
	public void requestTimeout(int requestTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setRequestTimeout(requestTimeout);
	}


	/**
	 */
	public void userAgent(String userAgent) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setUserAgent(userAgent);
	}


	/**
	 */
	public int connectionTTL() {
		return clientConfig.getConnectionTTL();
	}

	/**
	 */
	public int maxConnections() {
		return clientConfig.getMaxConnections();
	}

	/**
	 */
	public int maxConnectionsPerHost() {
		return clientConfig.getMaxConnectionsPerHost();
	}

	/**
	 */
	public int maxRequestRetry() {
		return clientConfig.getMaxRequestRetry();
	}

	/**
	 */
	public int pooledConnectionIdleTimeout() {
		return clientConfig.getPooledConnectionIdleTimeout();
	}

	/**
	 */
	public int readTimeout() {
		return clientConfig.getReadTimeout();
	}

	/**
	 */
	public String userAgent() {
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
