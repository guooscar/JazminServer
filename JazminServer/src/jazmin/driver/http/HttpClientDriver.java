package jazmin.driver.http;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

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
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(clientConfig),clientConfig);
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(HttpClientDriverCommand.class);
		}
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
				.print("asyncHttpProvider",clientConfig.getAsyncHttpProviderConfig())
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
				.print("isAcceptAnyCertificate",isAcceptAnyCertificate())
				.print("isAllowPoolingConnections",isAllowPoolingConnections())
				.print("isAllowPoolingSslConnections",isAllowPoolingSslConnections())
				.print("hostnameVerifier",getHostnameVerifier())
				.print("sslSessionCacheSize",getSslSessionCacheSize())
				.print("sslSessionTimeout",getSslSessionTimeout())
				
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
	 * @param hostnameVerifier
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setHostnameVerifier(javax.net.ssl.HostnameVerifier)
	 */
	public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		return clientConfigBuilder.setHostnameVerifier(hostnameVerifier);
	}
	/**
	 * @param sslSessionCacheSize
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setSslSessionCacheSize(java.lang.Integer)
	 */
	public Builder setSslSessionCacheSize(Integer sslSessionCacheSize) {
		return clientConfigBuilder.setSslSessionCacheSize(sslSessionCacheSize);
	}
	/**
	 * @param useProxyProperties
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setUseProxyProperties(boolean)
	 */
	public Builder setUseProxyProperties(boolean useProxyProperties) {
		return clientConfigBuilder.setUseProxyProperties(useProxyProperties);
	}
	/**
	 * @param useProxySelector
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setUseProxySelector(boolean)
	 */
	public Builder setUseProxySelector(boolean useProxySelector) {
		return clientConfigBuilder.setUseProxySelector(useProxySelector);
	}
	/**
	 * @param webSocketTimeout
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setWebSocketTimeout(int)
	 */
	public Builder setWebSocketTimeout(int webSocketTimeout) {
		return clientConfigBuilder.setWebSocketTimeout(webSocketTimeout);
	}
	/**
	 */
	public void setCompressionEnforced(boolean compressionEnforced) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setCompressionEnforced(compressionEnforced);
	}
	/**
	 */
	public void setConnectTimeout(int connectTimeOut) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectTimeout(connectTimeOut);
	}
	/**
	 */
	public void setConnectionTTL(int connectionTTL) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setConnectionTTL(connectionTTL);
	}

	/**
	 */
	public void setDisableUrlEncodingForBoundedRequests(
			boolean disableUrlEncodingForBoundedRequests) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setDisableUrlEncodingForBoundedRequests(disableUrlEncodingForBoundedRequests);
	}


	/**
	 * @param sslContext
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setSSLContext(javax.net.ssl.SSLContext)
	 */
	public Builder setSSLContext(SSLContext sslContext) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		return clientConfigBuilder.setSSLContext(sslContext);
	}
	/**
	 * @param sslSessionTimeout
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setSslSessionTimeout(java.lang.Integer)
	 */
	public Builder setSslSessionTimeout(Integer sslSessionTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		return clientConfigBuilder.setSslSessionTimeout(sslSessionTimeout);
	}
	/**
	 */
	public void setFollowRedirect(boolean followRedirect) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setFollowRedirect(followRedirect);
	}


	/**
	 */
	public void setMaxConnections(int maxConnections) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxConnections(maxConnections);
	}


	/**
	 */
	public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder
				.setMaxConnectionsPerHost(maxConnectionsPerHost);
	}

	/**
	 */
	public void setMaxRedirects(int maxRedirects) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setMaxRedirects(maxRedirects);
	}


	/**
	 */
	public void setMaxRequestRetry(int maxRequestRetry) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxRequestRetry(maxRequestRetry);
	}


	/**
	 */
	public void setPooledConnectionIdleTimeout(
			int pooledConnectionIdleTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder
				.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
	}


	/**
	 */
	public void setReadTimeout(int readTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		clientConfigBuilder.setReadTimeout(readTimeout);
	}


	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#isAcceptAnyCertificate()
	 */
	public boolean isAcceptAnyCertificate() {
		return clientConfig.isAcceptAnyCertificate();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#isAllowPoolingConnections()
	 */
	public boolean isAllowPoolingConnections() {
		return clientConfig.isAllowPoolingConnections();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#isAllowPoolingSslConnections()
	 */
	public boolean isAllowPoolingSslConnections() {
		return clientConfig.isAllowPoolingSslConnections();
	}
	/**
	 * @param acceptAnyCertificate
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setAcceptAnyCertificate(boolean)
	 */
	public void setAcceptAnyCertificate(boolean acceptAnyCertificate) {
		clientConfigBuilder
				.setAcceptAnyCertificate(acceptAnyCertificate);
	}
	/**
	 * @param allowPoolingConnections
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setAllowPoolingConnections(boolean)
	 */
	public void setAllowPoolingConnections(boolean allowPoolingConnections) {
		clientConfigBuilder
				.setAllowPoolingConnections(allowPoolingConnections);
	}
	/**
	 * @param allowPoolingSslConnections
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig.Builder#setAllowPoolingSslConnections(boolean)
	 */
	public void setAllowPoolingSslConnections(
			boolean allowPoolingSslConnections) {
		 clientConfigBuilder
				.setAllowPoolingSslConnections(allowPoolingSslConnections);
	}
	/**
	 */
	public void setRequestTimeout(int requestTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setRequestTimeout(requestTimeout);
	}


	/**
	 */
	public void setUserAgent(String userAgent) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setUserAgent(userAgent);
	}


	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getEnabledCipherSuites()
	 */
	public String[] getEnabledCipherSuites() {
		return clientConfig.getEnabledCipherSuites();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getEnabledProtocols()
	 */
	public String[] getEnabledProtocols() {
		return clientConfig.getEnabledProtocols();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getHostnameVerifier()
	 */
	public HostnameVerifier getHostnameVerifier() {
		return clientConfig.getHostnameVerifier();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getMaxRedirects()
	 */
	public int getMaxRedirects() {
		return clientConfig.getMaxRedirects();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getSslSessionCacheSize()
	 */
	public Integer getSslSessionCacheSize() {
		return clientConfig.getSslSessionCacheSize();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getSslSessionTimeout()
	 */
	public Integer getSslSessionTimeout() {
		return clientConfig.getSslSessionTimeout();
	}
	/**
	 * @return
	 * @see com.ning.http.client.AsyncHttpClientConfig#getWebSocketTimeout()
	 */
	public int getWebSocketTimeout() {
		return clientConfig.getWebSocketTimeout();
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
