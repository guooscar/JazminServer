/**
 * 
 */
package jazmin.server.cdn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.aop.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

/**
 * @author yama
 *
 */
public class CdnServer extends Server {
	private static Logger logger=LoggerFactory.get(CdnServer.class);
	static final String SERVER_NAME="jazmin-cdn-server";
	//
	private int port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	//
	private File homeDir;
	private boolean listDir;
	private boolean listDirInHtml;
	private URL orginSiteURL;
	private LongAdder requestIdGenerator;
	private Map<String,FileRequest>requests;
	//
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	//
	CachePolicy cachePolicy;
	//
	RequestFilter requestFilter;
	//
	public CdnServer() {
		port = 8001;
		requestIdGenerator=new LongAdder();
		requests=new ConcurrentHashMap<String, FileRequest>();
		asyncHttpClient = new AsyncHttpClient();
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setUserAgent(SERVER_NAME);
		clientConfigBuilder.setAsyncHttpClientProviderConfig(new NettyAsyncHttpProviderConfig());
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(clientConfig);
		listDir=true;
		listDirInHtml=true;
		cachePolicy=new CachePolicy();
	}
	//
	private void initNetty() throws Exception {
		IOWorker ioWorker = new IOWorker("CdnServerIO", Runtime.getRuntime()
				.availableProcessors() * 2 + 1);
		bossGroup = new NioEventLoopGroup(1, ioWorker);
		workerGroup = new NioEventLoopGroup(0, ioWorker);
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new HttpServerCodec());
						pipeline.addLast(new HttpObjectAggregator(65536));
						pipeline.addLast(new ChunkedWriteHandler());
						pipeline.addLast(new CdnServerHandler(CdnServer.this));
					}
				});

		b.bind(port).sync();
	}
	//
	private void stopNetty() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}
	
	/**
	 * @param type
	 * @param ttlInSeconds
	 * @see jazmin.server.cdn.CachePolicy#addPolicy(java.lang.String, int)
	 */
	public void addPolicy(String type, int ttlInSeconds) {
		cachePolicy.addPolicy(type, ttlInSeconds);
	}
	/**
	 * @return
	 * @see jazmin.server.cdn.CachePolicy#getPolicyMap()
	 */
	public Map<String, Long> getPolicyMap() {
		return cachePolicy.getPolicyMap();
	}
	/**
	 * @return the orginSiteURL
	 */
	public String getOrginSiteURL() {
		if(orginSiteURL==null){
			return null;
		}
		return orginSiteURL.toString();
	}
	/**
	 * @param orginSiteURL the orginSiteURL to set
	 * @throws MalformedURLException 
	 */
	public void setOrginSiteURL(String siteUrl) throws MalformedURLException {
		this.orginSiteURL = new URL(siteUrl);
	}
	/**
	 * @return the homeDir
	 */
	public String getHomeDir() {
		return homeDir.getAbsolutePath();
	}
	/**
	 * @param homeDir the homeDir to set
	 */
	public void setHomeDir(String homeDir) {
		if(isStarted()){
			throw new IllegalStateException("set before started");
		}
		File ff=new File(homeDir);
		if(ff.exists()&&!ff.isDirectory()){
			throw new IllegalArgumentException(homeDir+" is not directory");
		}
		if(!ff.exists()){
			if(!ff.mkdirs()){
				throw new IllegalArgumentException("can not create home dir "+homeDir);
			}	
		}
		this.homeDir = ff;	
	}
	/**
	 * @return the listDir
	 */
	public boolean isListDir() {
		return listDir;
	}
	/**
	 * @param listDir the listDir to set
	 */
	public void setListDir(boolean listDir) {
		this.listDir = listDir;
	}
	/**
	 * @return the listDirInHtml
	 */
	public boolean isListDirInHtml() {
		return listDirInHtml;
	}
	/**
	 * @param listDirInHtml the listDirInHtml to set
	 */
	public void setListDirInHtml(boolean listDirInHtml) {
		this.listDirInHtml = listDirInHtml;
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
		if(isStarted()){
			throw new IllegalStateException("set before started");
		}
		this.port = port;
	}

	/**
	 * @return the requestFilter
	 */
	public RequestFilter getRequestFilter() {
		return requestFilter;
	}
	/**
	 * @param requestFilter the requestFilter to set
	 */
	public void setRequestFilter(RequestFilter requestFilter) {
		this.requestFilter = requestFilter;
	}
	//--------------------------------------------------------------------------
	private Method requestWorkerMethod=Dispatcher.getMethod(
			RequestWorker.class,
			"processRequest");
	public void processRequest(ChannelHandlerContext ctx, FullHttpRequest request){
		FileRequest fileRequest=new FileRequest(this,request.uri(),ctx.channel(),request);
		requestIdGenerator.increment();
		fileRequest.id=requestIdGenerator.intValue()+"";
		requests.put(fileRequest.id, fileRequest);
		RequestWorker rw=new RequestWorker(this,fileRequest,ctx,request);
		Jazmin.dispatcher.invokeInPool(
				fileRequest.uri,
				rw,requestWorkerMethod);
	}
	//
	void removeFileRequest(String id) {
		requests.remove(id);
	}
	//
	List<FileRequest>getFileRequests(){
		return new ArrayList<FileRequest>(requests.values());
	}
	//-------------------------------------------------------------------------
	private void checkCachePolicy(){
		logger.info("clean expires file in {}",homeDir);
		cachePolicy.cleanFile(homeDir);
	}
	//
	@Override
	public void start() throws Exception {
		initNetty();
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(CdnServerCommand.class);
		}
		if(homeDir==null){
			try {
				homeDir=Files.createTempDirectory("JazminCdnServer").toFile();
				logger.info("home dir set to {}",homeDir);
			} catch (IOException e) {
				logger.warn("can not create default home dir");
			}	
		}
		if(orginSiteURL!=null){
			logger.info("set orgin site {} start cache check task.");
			Jazmin.scheduleAtFixedRate(this::checkCachePolicy,
					10,
					30, 
					TimeUnit.MINUTES);
		}
	}
	//
	@Override
	public void stop() throws Exception {
		stopNetty();
	}
	//
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("port",getPort())
		.print("homeDir",getHomeDir())
		.print("orginSiteURL",getOrginSiteURL())
		.print("listDir",isListDir())
		.print("listDirInHtml",isListDirInHtml())
		.print("requestFilter",getRequestFilter());
		
		for(Entry<String,Long> e:getPolicyMap().entrySet()){
			ib.print("policy-"+e.getKey(),e.getValue());
		}
		return ib.toString();
	}
}
