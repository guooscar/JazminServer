/**
 * 
 */
package jazmin.driver.influxdb;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.monitor.Monitor;
import jazmin.core.monitor.MonitorAgent;
import jazmin.driver.http.AllowAllHostnameVerifier;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;
import jazmin.util.JSONUtil;

/**
 * @author yama
 *
 */
public class InfluxdbDriver extends Driver{
	
	//
	private static final String DEFAULT_UA="JazminInfluxdbDriver";
	//
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	//
	LinkedList<String>errorLogs;
	AtomicLong queryCounter;
	AtomicLong writeCounter;
	AtomicLong errorCounter;
	
	//
	boolean useHttps;
	String user;
	String password;
	String database;
	String host;
	int port;
	String epoch;
	//
	public InfluxdbDriver() {
		asyncHttpClient = new AsyncHttpClient();
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setHostnameVerifier(new AllowAllHostnameVerifier());
		errorLogs=new LinkedList<String>();
		queryCounter=new AtomicLong();
		writeCounter=new AtomicLong();
		errorCounter=new AtomicLong();
		epoch="ms";
	}
	//
	@Override
	public void init() throws Exception {
		clientConfigBuilder.setUserAgent(DEFAULT_UA);
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(clientConfig),clientConfig);
		//
		if(host==null){
			throw new IllegalArgumentException("please setup server host");
		}
		if(port==0){
			throw new IllegalArgumentException("please setup server port");
		}
	}
	
	/**
	 * @return the useHttps
	 */
	public boolean isUseHttps() {
		return useHttps;
	}

	/**
	 * @param useHttps the useHttps to set
	 */
	public void setUseHttps(boolean useHttps) {
		this.useHttps = useHttps;
	}

	//
	public long getQueryCount(){
		return queryCounter.longValue();
	}
	//
	public long getWriteCount(){
		return writeCounter.longValue();
	}
	//
	public long getErrorCount(){
		return errorCounter.longValue();
	}
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}
	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	//
	@Override
	public void start() throws Exception {
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(InfluxdbDriverCommand.class);
		}
		Jazmin.mointor.registerAgent(new InfluxdbDriverMointorAgent());
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
				.print("user",getUser())
				.print("database",getDatabase())
				.print("host",getHost())
				.print("port",getPort())
				.print("useHttp",isUseHttps())
				.print("asyncHttpProvider",clientConfig.getAsyncHttpProviderConfig())
				.print("isCompressionEnforced",isCompressionEnforced())
				.print("maxConnections",getMaxConnections())
				.print("userAgent",getUserAgent())
				.print("readTimeout",getReadTimeout())
				.print("maxRequestRetry",getMaxRequestRetry())
				.print("connectionTTL",getConnectionTTL())
				.print("connectTimeout",getConnectTimeout())
				.print("isAcceptAnyCertificate",isAcceptAnyCertificate())
				.toString();			
	}
	/**
	 * execute SHOW DIAGNOSTICS command
	 * @param query
	 * @return
	 */
	public InfluxdbResponse showDiagnostics(){
		return query("SHOW DIAGNOSTICS");
	}
	/**
	 * execute SHOW STATS command
	 * @param query
	 * @return
	 */
	public InfluxdbResponse showStats(){
		return query("SHOW STATS");
	}
	//--------------------------------------------------------------------------
	
	public InfluxdbResponse query(String query){
		queryCounter.incrementAndGet();
		return execute0("query",query);
	}
	//
	public void write(String query){
		writeCounter.incrementAndGet();
		execute0("write",query);
	}
	//
	private InfluxdbResponse execute0(String type,String query){
		String url=useHttps?"http:":"http"+"://"+host+":"+port+"/"+type;
		
		if(type.equals("write")){
			if(database==null){
				throw new IllegalArgumentException("database can not be null");
			}
			url+="?db="+database;
		}
		//
		BoundRequestBuilder builder=asyncHttpClient.preparePost(url);
		if(user!=null){
			String up=user+":"+password;
			up=new String(Base64.getEncoder().encode(up.getBytes()));
			builder.addHeader("Authorization","Basic "+up);
		}
		//
		if(type.equals("query")){
			if(database!=null){
				builder.addFormParam("db", database);
			}	
			builder.addFormParam("epoch", epoch);
			builder.addFormParam("q", query);
		}
		if(type.equals("write")){
			builder.setBody(query.getBytes());
		}
		//
		Request requset=builder.build();
		ListenableFuture<Response>rsp=asyncHttpClient.executeRequest(requset);
		Response response;
		try {
			response=rsp.get();
		} catch(Exception e){
			throw new InfluxdbException(e);
		}
		int statusCode=response.getStatusCode();
		//https://docs.influxdata.com/influxdb/v1.1/guides/writing_data/
		/* 2xx: If your write request received HTTP 204 No Content, it was a success!
		 * If it’s HTTP 200 OK, InfluxDB understood the request but couldn’t complete it. The body of the response will 
		 * contain additional error information.
		 * 4xx: InfluxDB could not understand the request.
		 * 5xx: The system is overloaded or significantly impaired.
		 */
		String resultMessage="";
		try {
			resultMessage = response.getResponseBody();
		} catch (IOException e) {
			resultMessage=e.getMessage();
		}
		//
		if(statusCode>=400&&statusCode<500){
			errorCounter.incrementAndGet();
			throw new InfluxdbException("InfluxDB could not understand the request."+		
					resultMessage);
		}
		//
		if(statusCode>=500){
			errorCounter.incrementAndGet();
			throw new InfluxdbException("The system is overloaded or significantly impaired."+		
					resultMessage);
		}
		//
		if(statusCode>=200&&statusCode<300){
			InfluxdbResponse ret=JSONUtil.fromJson(resultMessage,InfluxdbResponse.class);
			return ret;
		}
		throw new IllegalArgumentException("unknow status code");
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
	public void setMaxConnections(int maxConnections) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setMaxConnections(maxConnections);
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
	 */
	public void setRequestTimeout(int requestTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		 clientConfigBuilder.setRequestTimeout(requestTimeout);
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
	public int getMaxRequestRetry() {
		return clientConfig.getMaxRequestRetry();
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
	//--------------------------------------------------------------------------------------------


	
	//--------------------------------------------------------------------------------------------
	//
	//
	private class InfluxdbDriverMointorAgent implements MonitorAgent{
		@Override
		public void sample(int idx,Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			info.put("count",queryCounter.longValue()+"");
			monitor.sample("InfluxdbDriver.QueryCount",Monitor.CATEGORY_TYPE_COUNT,info);
			//
			Map<String,String>info2=new HashMap<String, String>();
			info2.put("count",writeCounter.longValue()+"");
			monitor.sample("InfluxdbDriver.WriteCount",Monitor.CATEGORY_TYPE_COUNT,info2);
			//
			Map<String,String>info3=new HashMap<String, String>();
			info3.put("count",errorCounter.longValue()+"");
			monitor.sample("InfluxdbDriver.ErrorCount",Monitor.CATEGORY_TYPE_COUNT,info3);
		}
		//
		@Override
		public void start(Monitor monitor) {
			
		}
		
	}
}
