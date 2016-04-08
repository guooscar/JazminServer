package jazmin.server.file;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;

import jazmin.misc.io.IOWorker;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class FileClient {
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	HttpDataFactory factory;
	//
	public FileClient() {
		initNettyConnector();
	}
	//
	public void stop(){
		factory.cleanAllHttpData();
		group.shutdownGracefully();
	}
	//
	private void initNettyConnector(){
		IOWorker worker=new IOWorker("FileClientIO",1);
		group = new NioEventLoopGroup(1,worker);
		bootstrap = new Bootstrap();
		factory= new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); 
		// Disk if MINSIZE exceed
	    DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
	    DiskFileUpload.baseDirectory = null; // system temp directory
	    DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
	    DiskAttribute.baseDirectory = null; // system temp directory
		//clientHandler=new RpcClientHandler(this);
		ChannelInitializer <SocketChannel>channelInitializer=
				new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("codec", new HttpClientCodec());
		        pipeline.addLast("inflater", new HttpContentDecompressor());
		        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		        pipeline.addLast("handler", new HttpUploadClientHandler());
			}
		};
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32*1024) 
        .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8*1024)
		.handler(channelInitializer);
	}
	//
	public String formpost(
            String url,
            File file) throws Exception {
		URI simpleURI=new URI(url);
		String host=simpleURI.getHost();
        int port=simpleURI.getPort();
        String path=simpleURI.getPath();
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        Channel channel = future.sync().channel();
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, true); 
        bodyRequestEncoder.addBodyAttribute("getform", "POST");;
        bodyRequestEncoder.addBodyFileUpload("file", file, "application/x-zip-compressed", false);
        request = bodyRequestEncoder.finalizeRequest();
        channel.write(request);
        if (bodyRequestEncoder.isChunked()) { 
            channel.write(bodyRequestEncoder);
        }
        channel.flush();
        channel.closeFuture().sync();
        String result=channel.attr(HttpUploadClientHandler.ATTR_RESULT).get();
        return result;
    }
	//
	public static void main(String[] args)throws Exception {
		FileClient fc=new FileClient();
		String s=fc.formpost("http://localhost:8080/upload/",new File("/Users/yama/Desktop/book-of-vaadin-zh.pdf"));
		System.err.println(s);
		System.out.println("xxxxxb");
	}
}
