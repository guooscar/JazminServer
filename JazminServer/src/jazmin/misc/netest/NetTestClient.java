/**
 * 
 */
package jazmin.misc.netest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author yama 8 May, 2015
 */
public class NetTestClient {
	//
	static EventLoopGroup group = new NioEventLoopGroup();
	Channel channel;
	protected String host;
	protected int port;
	//
	public void connect(String host, int port) throws Exception {
		this.host=host;
		this.port=port;
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new NetTestHandler(NetTestClient.this));
					}
				});
		ChannelFuture f = b.connect(host, port).sync();
		channel = f.channel();
	}
	//
	public void send(ByteBuf buffer){
		channel.writeAndFlush(buffer);	
	}
	//
	public void close()throws Exception{
		channel.closeFuture().sync();
	}
	//
	public void onConnect()throws Exception{
		
	}
	//
	public void onDisConnect()throws Exception{
		
	}
	//
	public void onMessage(ByteBuf buffer)throws Exception{
		
	}
}
