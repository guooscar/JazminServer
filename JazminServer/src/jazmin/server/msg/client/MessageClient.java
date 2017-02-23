/**
 * 
 */
package jazmin.server.msg.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.IOWorker;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.MessageEncoder;
import jazmin.server.msg.CodecFactory;
import jazmin.server.msg.codec.DefaultCodecFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;

/**
 * @author yama
 */
public class MessageClient {
	private static Logger logger=LoggerFactory.get(MessageClient.class);
	//
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	private NetworkTrafficStat networkTrafficStat;
	private Channel channel;
	private CodecFactory codecFactory;
	//
	public MessageClient() {
		networkTrafficStat=new NetworkTrafficStat();
		codecFactory=new DefaultCodecFactory();
		initNettyConnector();
	}
	//
	private void initNettyConnector(){
		IOWorker worker=new IOWorker("MessageClientIO",1);
		group = new NioEventLoopGroup(1,worker);
		bootstrap = new Bootstrap();
		MessageClientHandler clientHandler=new MessageClientHandler(this);
		ChannelInitializer <SocketChannel>channelInitializer=
				new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast(
							new MessageEncoder(codecFactory,networkTrafficStat), 
							new MessageEncoder(codecFactory,networkTrafficStat),
							clientHandler);
				
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
	public void connect(String host,int port){
		try {
			if(logger.isWarnEnabled()){
				logger.warn("connect message server {}:{}",host,port);
			}
			channel=bootstrap.connect(host, port).sync().channel();
		} catch (Exception e) {
			logger.error("can not connect to server "+host+":"+port,e);
		}
	}
	public void messageRecieved(ResponseMessage rspMessage) {
		logger.debug("<<<<<<<<\n"+DumpUtil.dump(rspMessage));
	}
	//
	public void send(RequestMessage requestMessage){
		logger.debug(">>>>>>>>\n"+DumpUtil.dump(requestMessage));
		channel.writeAndFlush(requestMessage);
	}
}
