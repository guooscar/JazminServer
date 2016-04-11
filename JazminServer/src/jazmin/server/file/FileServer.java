/**
 * 
 */
package jazmin.server.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.thread.Dispatcher;
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
public class FileServer extends Server {
	private static Logger logger=LoggerFactory.get(FileServer.class);
	static final String SERVER_NAME="jazmin-file-server";
	//
	private int port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	//
	private File homeDir;
	private AtomicLong requestIdGenerator;
	private Map<String,FileOpt>requests;
	//
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	private DirectioryPrinter directioryPrinter;
	//
	RequestFilter requestFilter;
	//
	public FileServer() {
		port = 9001;
		requestIdGenerator=new AtomicLong();
		requests=new ConcurrentHashMap<String, FileOpt>();
		asyncHttpClient = new AsyncHttpClient();
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setUserAgent(SERVER_NAME);
		clientConfigBuilder.setAsyncHttpClientProviderConfig(new NettyAsyncHttpProviderConfig());
		directioryPrinter=new HtmlDirectoryPrinter();
	}
	//
	private void initNetty() throws Exception {
		IOWorker ioWorker = new IOWorker("FileServerIO", Runtime.getRuntime()
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
						pipeline.addLast(new ChunkedWriteHandler());
						pipeline.addLast(new FileServerHandler(FileServer.this));
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
	 * @return the directioryPrinter
	 */
	public DirectioryPrinter getDirectioryPrinter() {
		return directioryPrinter;
	}
	/**
	 * @param directioryPrinter the directioryPrinter to set
	 */
	public void setDirectioryPrinter(DirectioryPrinter directioryPrinter) {
		this.directioryPrinter = directioryPrinter;
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
	
	private Method handleHttpContentMethod=Dispatcher.getMethod(
			RequestWorker.class,
			"handleHttpContent",DefaultHttpContent.class);
	//
	public static final AttributeKey<RequestWorker> WORKER_KEY=
			AttributeKey.valueOf("s");
	//
	public void processRequest(ChannelHandlerContext ctx, HttpObject obj){
		if(obj instanceof DefaultHttpRequest){
			DefaultHttpRequest request=(DefaultHttpRequest) obj;
			String requestURI=request.uri();
			HttpMethod method=request.method();
			//
			if(requestURI.startsWith("/download/")&&
					method.equals(io.netty.handler.codec.http.HttpMethod.GET)){
				FileDownload fileRequest=new FileDownload(this,requestURI,ctx.channel(),request);
				fileRequest.id=requestIdGenerator.incrementAndGet()+"";
				requests.put(fileRequest.id, fileRequest);
				GetRequestWorker rw=new GetRequestWorker(this,fileRequest,ctx,request);
				ctx.channel().attr(WORKER_KEY).set(rw);
				Jazmin.dispatcher.invokeInPool(
						requestURI,
						rw,requestWorkerMethod);
				return;
			}
			//
			if(requestURI.startsWith("/upload/")&&
					(method.equals(io.netty.handler.codec.http.HttpMethod.POST)||
					method.equals(io.netty.handler.codec.http.HttpMethod.PUT))){
				FileUpload upload=new FileUpload(this,requestURI,ctx.channel(),request);
				upload.id=requestIdGenerator.incrementAndGet()+"";
				requests.put(upload.id, upload);
				PostRequestWorker worker=new PostRequestWorker(this,ctx,request,upload);
				ctx.channel().attr(WORKER_KEY).set(worker);
				worker.request=request;
				Jazmin.dispatcher.invokeInPool(
						requestURI,
						worker,requestWorkerMethod,Dispatcher.EMPTY_CALLBACK);
				return;
			}
			//
			OtherRequestWorker rw=new OtherRequestWorker(this,ctx,request);
			Jazmin.dispatcher.invokeInPool(
					requestURI,
					rw,requestWorkerMethod);
			return;
		}
		if(obj instanceof DefaultHttpContent){
			RequestWorker rw=ctx.channel().attr(WORKER_KEY).get();
			DefaultHttpContent dhc=(DefaultHttpContent) obj;
			Jazmin.dispatcher.invokeInCaller(
					rw.request.uri(),
					rw,handleHttpContentMethod,Dispatcher.EMPTY_CALLBACK,dhc);
		}
	}
	//
	void removeFileRequest(String id) {
		requests.remove(id);
	}
	//
	List<FileOpt>getFileRequests(){
		return new ArrayList<FileOpt>(requests.values());
	}
	//-------------------------------------------------------------------------
	
	//
	public void init(){
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(clientConfig);
	}
	//
	@Override
	public void start() throws Exception {
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(FileServerCommand.class);
		}
		if(homeDir==null){
			try {
				homeDir=Files.createTempDirectory("JazminCdnServer").toFile();
				logger.info("home dir set to {}",homeDir);
			} catch (IOException e) {
				logger.warn("can not create default home dir");
			}	
		}
		initNetty();
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
		.print("directioryPrinter",getDirectioryPrinter())
		.print("requestFilter",getRequestFilter());
		
		return ib.toString();
	}
}
