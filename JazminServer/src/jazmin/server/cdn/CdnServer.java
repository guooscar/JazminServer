/**
 * 
 */
package jazmin.server.cdn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

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
import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class CdnServer extends Server {
	private int port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	//
	private String homeDir;
	private LongAdder requestIdGenerator;
	private Map<String,FileRequest>requests;
	//
	public CdnServer() {
		port = 8001;
		requestIdGenerator=new LongAdder();
		requests=new ConcurrentHashMap<String, FileRequest>();
		homeDir="./";
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
	 * @return the homeDir
	 */
	public String getHomeDir() {
		return homeDir;
	}
	/**
	 * @param homeDir the homeDir to set
	 */
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}
	//--------------------------------------------------------------------------
	public void processRequest(ChannelHandlerContext ctx, FullHttpRequest request){
		FileRequest fileRequest=new FileRequest(this,request.uri(),ctx.channel());
		requestIdGenerator.increment();
		fileRequest.id=requestIdGenerator.intValue()+"";
		requests.put(fileRequest.id, fileRequest);
		RequestWorker rw=new RequestWorker(fileRequest);
		try {
			rw.processRequest(ctx, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//
	void removeFileRequest(String id) {
		requests.remove(id);
	}
	//
	List<FileRequest>getFileRequests(){
		return new ArrayList<FileRequest>(requests.values());
	}
	// --------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		initNetty();
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new CdnServerCommand());
		}
	}
	//
	@Override
	public void stop() throws Exception {
		stopNetty();
	}

	//
	@Override
	public String info() {
		return super.info();
	}
}
