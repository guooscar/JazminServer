/**
 * 
 */
package jazmin.server.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.IOWorker;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.codec.fst.FSTDecoder;
import jazmin.server.rpc.codec.fst.FSTEncoder;
import jazmin.server.rpc.codec.json.JSONDecoder;
import jazmin.server.rpc.codec.json.JSONEncoder;
import jazmin.server.rpc.codec.zjson.CompressedJSONDecoder;
import jazmin.server.rpc.codec.zjson.CompressedJSONEncoder;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class RPCClient {
	private static Logger logger=LoggerFactory.get(RPCClient.class);
	//
	public static final AttributeKey<RPCSession> SESSION_KEY=
										AttributeKey.valueOf("rpcsession");
	//
	private EventLoopGroup group;
	private RPCClientHandler clientHandler;
	private Bootstrap bootstrap;
	private RPCMessageCallback pushMessageCallback;
	private NetworkTrafficStat networkTrafficStat;
	//
	//
	private static final int DEFAULT_TIMEOUT=15000;//15 sec
	//
	private AtomicInteger messageId;
	private Map<Integer,RPCLock>lockMap;
	private long timeout;
	private String principal;
	//
	//
	static class RPCLock{
		public int id;
		public long startTime;
		public RPCMessage response;
		public RPCMessageCallback asyncCallback;
	}
	//
	public RPCClient() {
		messageId=new AtomicInteger();
		lockMap=new ConcurrentHashMap<Integer, RPCLock>();
		timeout=DEFAULT_TIMEOUT;
		networkTrafficStat=new NetworkTrafficStat();
		initNettyConnector();
		Jazmin.scheduleAtFixedRate(
				this::checkAsyncTimeout,
				2000, 2000, TimeUnit.MILLISECONDS);
		principal=Jazmin.getServerName();
	}
	//
	
	//
	public void setPrincipal(String p){
		this.principal=p;
	}
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	//
	public String getPrincipal(){
		return principal;
	}
	//
	private void checkAsyncTimeout(){
		lockMap.forEach((id,lock)->{
			if(lock.asyncCallback!=null&&
					(System.currentTimeMillis()-lock.startTime)>timeout){
				Throwable e=new RPCException("rpc request:"+lock.id+" timeout");
				RPCMessage msg=new RPCMessage();
				msg.id=id;
				msg.type=RPCMessage.TYPE_RPC_CALL_RSP;
				msg.payloads=new Object[]{null,e};
				lock.asyncCallback.callback(null,msg);
				lockMap.remove(id);
			}
		});
	}
	//
	public void stop(){
		group.shutdownGracefully();
	}
	//
	private void initNettyConnector(){
		IOWorker worker=new IOWorker("RPCClientIO",1);
		group = new NioEventLoopGroup(1,worker);
		bootstrap = new Bootstrap();
		clientHandler=new RPCClientHandler(this);
		ChannelInitializer <SocketChannel>channelInitializer=
				new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel sc) throws Exception {
				if(RPCServer.codec==RPCServer.CODEC_ZJSON){
					sc.pipeline().addLast(
							new CompressedJSONEncoder(networkTrafficStat), 
							new CompressedJSONDecoder(networkTrafficStat),
							clientHandler);		
				}else if(RPCServer.codec==RPCServer.CODEC_JSON){
					sc.pipeline().addLast(
							new JSONEncoder(networkTrafficStat), 
							new JSONDecoder(networkTrafficStat),
							clientHandler);
				}else if(RPCServer.codec==RPCServer.CODEC_FST){
					sc.pipeline().addLast(
							new FSTEncoder(networkTrafficStat), 
							new FSTDecoder(networkTrafficStat),
							clientHandler);
				}else{
					throw new IllegalArgumentException("bad codec type:"+RPCServer.codec);
				}
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
	/**
	 * create a new RPC session using specified host and port.
	 */
	public void connect(RPCSession session){
		String host=session.getRemoteHostAddress();
		int port=session.getRemotePort();
		try {
			if(logger.isWarnEnabled()){
				logger.warn("connect rpc server {}:{}",host,port);
			}
			Channel channel=bootstrap.connect(host, port).sync().channel();
			channel.attr(SESSION_KEY).set(session);
			session.setChannel(channel);
			session.setPrincipal(principal);
			//
			//send auth message
			auth(session);
		} catch (Exception e) {
			logger.error("can not connect to server "+host+":"+port,e);
		}
	}
	/**/
	private void auth(RPCSession session){
		RPCMessage msg=new RPCMessage();
		msg.id=messageId.incrementAndGet();
		msg.type=RPCMessage.TYPE_SESSION_AUTH;
		msg.payloads=new Object[session.getTopics().size()+3];
		msg.payloads[0]=session.getPrincipal();
		msg.payloads[1]=session.getCredential();
		msg.payloads[2]=session.disablePushMessage;
		int idx=3;
		for(String s:session.getTopics()){
			msg.payloads[idx++]=s;
		}
		session.write(msg);
	}
	//--------------------------------------------------------------------------
	//message
	/**
	 * 
	 */
	public void messageRecieved(RPCSession session,RPCMessage message){
		switch (message.type) {
		case RPCMessage.TYPE_RPC_CALL_RSP:
			rpcRspMessageReceived(session,message);
			break;
		case RPCMessage.TYPE_PUSH:
			pushMessageReceived(session,message);
			break;
		default:
			logger.warn("bad message type:"+message);
			break;
		}
	}
	//
	private void pushMessageReceived(RPCSession session,RPCMessage msg){
		if(pushMessageCallback!=null){
			pushMessageCallback.callback(session, msg);
		}
	}
	//
	private void rpcRspMessageReceived(RPCSession session,RPCMessage msg){
		RPCLock lock=lockMap.get(msg.id);
		if(lock==null){
			logger.warn("request:"+msg.id+" already timeout");
			return;
		}
		lock.response=msg;
		//
		if(lock.asyncCallback==null){
			//sync invoke
			synchronized (lock) {
				lock.notifyAll();
			}
		}else{
			lockMap.remove(lock.id);
			lock.asyncCallback.callback(session,lock.response);
		}		
	}
	//
	/**
	 */
	public int sendMessage(int msgId,RPCSession session,String serviceId,Object args[]){
		RPCMessage msg=new RPCMessage();
		msg.id=msgId;
		msg.type=RPCMessage.TYPE_RPC_CALL_REQ;
		int argsLength=(args==null)?0:args.length;
		msg.payloads=new Object[argsLength+1];
		msg.payloads[0]=serviceId;
		if(argsLength>0){
			System.arraycopy(args, 0, msg.payloads,1,argsLength);
		}
		session.write(msg);
		return msg.id;
	}
	/**
	 * sync invoke service
	 */
	public RPCMessage invokeSync(RPCSession session,String serviceId,Object args[]){
		RPCLock lock=new RPCLock();
		lock.startTime=System.currentTimeMillis();
		int nextMsgId=messageId.incrementAndGet();
		lock.id=nextMsgId;
		lockMap.put(lock.id,lock);
		sendMessage(nextMsgId,session,serviceId, args);
		synchronized (lock) {
			try {
				while(lock.response==null){
					lock.wait(timeout/2);//wait 10 seconds
					long currentTime=System.currentTimeMillis();
					if((currentTime-lock.startTime)>timeout){
						lockMap.remove(lock.id);
						throw new RPCException(
								"rpc request:"+lock.id+" timeout/");
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			lockMap.remove(lock.id);
			return lock.response;
		}
	}
	/**
	 * async invoke service  
	 */
	public void invokeAsync(
			RPCSession session,
			String serviceId,
			Object args[],
			RPCMessageCallback callback){
		RPCLock lock=new RPCLock();
		lock.startTime=System.currentTimeMillis();
		lock.id=messageId.incrementAndGet();
		lock.asyncCallback=callback;
		lockMap.put(lock.id,lock);
		sendMessage(lock.id,session,serviceId, args);
	}
	//
	/**
	 * @return the pushMessageCallback
	 */
	public RPCMessageCallback getPushMessageCallback() {
		return pushMessageCallback;
	}
	/**
	 * @param pushMessageCallback the pushMessageCallback to set
	 */
	public void setPushMessageCallback(RPCMessageCallback pushMessageCallback) {
		this.pushMessageCallback = pushMessageCallback;
	}
	
}
