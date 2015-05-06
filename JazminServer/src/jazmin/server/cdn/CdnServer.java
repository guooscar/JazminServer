/**
 * 
 */
package jazmin.server.cdn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import jazmin.core.Server;
import jazmin.misc.io.IOWorker;

/**
 * @author yama
 *
 */
public class CdnServer extends Server {
	private int port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	//
	public CdnServer() {
		port = 8001;
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
						pipeline.addLast(new CdnServerHandler());
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

	// --------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		initNetty();
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

	// --------------------------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CdnServer server = new CdnServer();
		server.start();
	}

}
