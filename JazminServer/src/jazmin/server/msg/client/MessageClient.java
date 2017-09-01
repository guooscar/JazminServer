/**
 * 
 */
package jazmin.server.msg.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import jazmin.server.msg.codec.DefaultCodecFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.rpc.RpcException;
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
	private Map<Integer,RPCLock> lockMap;
	private int timeout=5000;//5 sec timeout
	private AtomicInteger messageId;
	//
	public static int MESSAGE_TYPE=DefaultCodecFactory.FORMAT_JSON;
	//
	public MessageClient() {
		networkTrafficStat=new NetworkTrafficStat();
		lockMap=new ConcurrentHashMap<>();
		messageId=new AtomicInteger(1);
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
							new MessageEncoder(networkTrafficStat), 
							new MessageDecoder(networkTrafficStat),
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
		RPCLock lock=lockMap.get(rspMessage.requestId);
		if(lock!=null){
			synchronized (lock) {
				lock.response=rspMessage;
				lock.notifyAll();
			}
		}
	}
	//
	public void send(int msgId,String serviceId,String[] args){
		RequestMessage msg=new RequestMessage();
		msg.requestId=msgId;
		msg.messageType=MESSAGE_TYPE;
		msg.serviceId=serviceId;
		if(args!=null){
			msg.requestParameters=args;
		}
		if(logger.isDebugEnabled()){
			logger.debug(">>>>>>>>\n"+DumpUtil.dump(msg));
		}
		channel.writeAndFlush(msg);
	}
	//
	static class RPCLock{
		public int id;
		public long startTime;
		public ResponseMessage response;
	}
	//
	public ResponseMessage invokeSync(String serviceId,String[] args){
		RPCLock lock=new RPCLock();
		lock.startTime=System.currentTimeMillis();
		lock.id=messageId.incrementAndGet();
		lockMap.put(lock.id,lock);
		send(lock.id,serviceId,args);
		synchronized (lock) {
			try {
				while(lock.response==null){
					lock.wait(timeout);
					long currentTime=System.currentTimeMillis();
					if((currentTime-lock.startTime)>timeout){
						lockMap.remove(lock.id);
						throw new RpcException(
								"rpc request:"+lock.id+" timeout,startTime:"+lock.startTime+",serviceId:"+serviceId);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			lockMap.remove(lock.id);
			return lock.response;
		}
	}
}
