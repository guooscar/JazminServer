/**
 * 
 */
package jazmin.server.rpc;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;
import jazmin.core.Jazmin;
import jazmin.core.Registerable;
import jazmin.core.Server;
import jazmin.core.app.AppException;
import jazmin.core.monitor.Monitor;
import jazmin.core.monitor.MonitorAgent;
import jazmin.core.thread.DispatcherCallbackAdapter;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rpc.RpcMessage.AppExceptionMessage;
import jazmin.server.rpc.codec.fst.FSTDecoder;
import jazmin.server.rpc.codec.fst.FSTEncoder;
import jazmin.server.rpc.codec.json.JSONDecoder;
import jazmin.server.rpc.codec.json.JSONEncoder;
import jazmin.server.rpc.codec.kyro.KyroDecoder;
import jazmin.server.rpc.codec.kyro.KyroEncoder;
import jazmin.server.rpc.codec.zjson.CompressedJSONDecoder;
import jazmin.server.rpc.codec.zjson.CompressedJSONEncoder;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class RpcServer extends Server implements Registerable{
	private static Logger logger=LoggerFactory.get(RpcServer.class);
	//
	private int port=6001;
	private boolean enableSSL;
	private int idleTime=60*60;//one hour
	private String certificateFile;
	private String privateKeyFile;
	private String privateKeyPhrase;
	private String credential;
	private ServerBootstrap nettyServer;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private RpcServerHandler rpcServerHandler;
	private Map<String,Object>instanceMap;
	private Map<String,Method>methodMap;
	private Map<String,RpcSession>sessionMap;
	private Map<String,List<RpcSession>>topicSessionMap;
	private Map<String,LongAdder>pushMessageCountMap;
	
	private NetworkTrafficStat networkTrafficStat;
	private Set<String>acceptRemoteHosts;
	private IOWorker ioWorker;
	private SslContext sslContext;
	//
	private static ThreadLocal<RpcSession>rpcSessionThreadLocal=new ThreadLocal<RpcSession>();
	//
	public static final int CODEC_JSON=1;
	public static final int CODEC_ZJSON=2;
	public static final int CODEC_FST=3;
	public static final int CODEC_KYRO=4;
	
	public static int codec=CODEC_ZJSON;
	//
	public RpcServer() {
		nettyServer=new ServerBootstrap();
		instanceMap=new ConcurrentHashMap<String, Object>();
		methodMap=new ConcurrentHashMap<String, Method>();
		sessionMap=new ConcurrentHashMap<String, RpcSession>();
		topicSessionMap=new ConcurrentHashMap<String, List<RpcSession>>();
		pushMessageCountMap=new ConcurrentHashMap<String, LongAdder>();
		acceptRemoteHosts=Collections.synchronizedSet(new TreeSet<String>());
		networkTrafficStat=new NetworkTrafficStat();
		enableSSL=false;
		certificateFile="";
		privateKeyFile="";
		privateKeyPhrase="";
	}
	//--------------------------------------------------------------------------
	@Override
	public void register(Object object) {
		Class<?>implClass=object.getClass();
		if(implClass.getAnnotation(RpcService.class)!=null){
			registerService(object);
		}
	}
	//instance
	/**
	 * register remote service,remote service object must have an interface of 
	 * remote service
	 * @param instance the remote service instance
	 */
	public void registerService(Object instance){
		Class<?>implClass=instance.getClass();
		Class<?>[]interfaces=instance.getClass().getInterfaces();
		if(interfaces.length!=1){
			throw new IllegalArgumentException(implClass+
					" should have one remote interface");
		}
		//
		Class<?>interfaceClass=interfaces[0];
		//
		String instanceName=interfaceClass.getSimpleName();
		if(instanceMap.containsKey(instanceName)){
			throw new IllegalArgumentException("instance:"+instanceName
					+" already exites.");
		}
		logger.debug("register instance:{}",instanceName);
		instanceMap.put(instanceName, instance);
		//
		//
		for(Method m:implClass.getMethods()){
			//Transaction annotation add on impl class so we should use implClass
			if(!Modifier.isPublic(m.getModifiers())){
				continue;
			}
			if(Modifier.isStatic(m.getModifiers())){
				continue;
			}
			//
			String methodName=instanceName+"."+m.getName();
			if(methodMap.containsKey(methodName)){
				throw new IllegalArgumentException("method:"+methodName
						+" already exists.");
			}
			logger.debug("register method:{}",methodName);
			methodMap.put(methodName, m);
		}
	}
	/**
	 * return all service names
	 * @return all service names
	 */
	public List<String>getServiceNames(){
		return new ArrayList<String>(methodMap.keySet());
	}
	/**
	 * return all topic names
	 * @return all topic names
	 */
	public List<String>getTopicNames(){
		return new ArrayList<String>(topicSessionMap.keySet());
	}
	/**
	 * return all session releated to specified topic name
	 * @param name topic name
	 * @return all session releated to specified topic name
	 */
	public List<RpcSession>getTopicSession(String name){
		return topicSessionMap.get(name);
	}
	/**
	 * return push message stat count map
	 * @return push message stat count map
	 */
	public Map<String,LongAdder>getPushMessageCountMap(){
		return new HashMap<String, LongAdder>(pushMessageCountMap);
	}
	//
	/**
	 * return rpc session of current request
	 * @return rpc session of current request 
	 */
	public static RpcSession getSession(){
		return rpcSessionThreadLocal.get();
	}
	//--------------------------------------------------------------------------
	//
	IOWorker getIOWorker(){
		return ioWorker;
	}
	//io
	private void initNettyConnector(){
		rpcServerHandler=new RpcServerHandler(this);
		//
		ChannelInitializer<SocketChannel> channelInitializer
			=new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				if(enableSSL){
	                SslContext sslContext=createSslContext();
	                sc.pipeline().addLast(sslContext.newHandler(sc.alloc()));	
	            }
				sc.pipeline().addLast("idleStateHandler",
						new IdleStateHandler(idleTime,idleTime,0));
				if(codec==CODEC_ZJSON){
					sc.pipeline().addLast(
							new CompressedJSONEncoder(networkTrafficStat), 
							new CompressedJSONDecoder(networkTrafficStat),
							rpcServerHandler);		
				}else if(codec==CODEC_JSON){
					sc.pipeline().addLast(
							new JSONEncoder(networkTrafficStat), 
							new JSONDecoder(networkTrafficStat),
							rpcServerHandler);
				}else if(codec==CODEC_FST){
					sc.pipeline().addLast(
							new FSTEncoder(networkTrafficStat), 
							new FSTDecoder(networkTrafficStat),
							rpcServerHandler);
				}else if(codec==CODEC_KYRO){
					sc.pipeline().addLast(
							new KyroEncoder(networkTrafficStat), 
							new KyroDecoder(networkTrafficStat),
							rpcServerHandler);
				}else{
					throw new IllegalArgumentException("bad codec type:"+RpcServer.codec);
				}
			}
		};
		//
		ioWorker=new IOWorker("RPCServerIO",Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup = new NioEventLoopGroup(1,ioWorker);
		workerGroup = new NioEventLoopGroup(0,ioWorker);
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 128)    
		.option(ChannelOption.SO_RCVBUF, 1024*256)   
		.option(ChannelOption.SO_SNDBUF, 1024*256)  
		.option(ChannelOption.SO_REUSEADDR, true)   
        .childOption(ChannelOption.SO_KEEPALIVE, true) 
        .childOption(ChannelOption.TCP_NODELAY, true) 
        .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32*1024) 
        .childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8*1024)
        .childHandler(channelInitializer);
	}
	//
	//
    private SslContext createSslContext()throws Exception{
		if(sslContext!=null){
			return sslContext;
		}
		File certiFile = new File(certificateFile);
		File keyFile = new File(privateKeyFile);
		if (certiFile.exists() && keyFile.exists()) {
			sslContext=SslContextBuilder.forServer(new File(certificateFile),
					new File(privateKeyFile), privateKeyPhrase).build();
		} else {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslContext=SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();
	
			logger.warn("using SelfSignedCertificate.only for debug mode");
		}
		return sslContext;
    }
	//--------------------------------------------------------------------------
	//message
	void messageReceived(RpcSession session,RpcMessage message){
		switch (message.type) {
		case RpcMessage.TYPE_RPC_CALL_REQ:
			rpcRequestCallMessageReceived(session,message);
			break;
		case RpcMessage.TYPE_SESSION_AUTH:
			authMessageReceived(session,message);
			break;
		case RpcMessage.TYPE_HEARTBEAT:
			sendHeartbeat(session,message);
			break;
		default:
			logger.error("bad message type:"+message);
			break;
		}
	}
	//
	private void sendHeartbeat(RpcSession session,RpcMessage message){
		synchronized (session) {
			if(logger.isDebugEnabled()){
				logger.debug("receive heart beat from {}",session.getPrincipal());
			}
			RpcMessage msg=new RpcMessage();
			msg.id=message.id;
			msg.type=RpcMessage.TYPE_HEARTBEAT;
			session.write(msg);
		}
	}
	//
	private void authMessageReceived(RpcSession session,RpcMessage message){
		synchronized (session) {
			String principal=(String)message.payloads[0];
			String credential=(String)message.payloads[1];
			Boolean disablePush=(Boolean)message.payloads[2];
			Object topics[]=new Object[message.payloads.length-3];
			if(topics.length>0){
				System.arraycopy(message.payloads,3, topics, 0, topics.length);
			}
			//
			session.setPrincipal(principal);
			session.setCredential(credential);
			session.setDisablePushMessage(disablePush);
			for(Object o:topics){
				session.subscribe((String)o);
			}
			checkCredential(session);
			sessionCreated(session);
		}
	}
	//
	private void checkCredential(RpcSession session){
		if(this.credential==null){
			session.authed=true;
			return;
		}
		session.authed=credential.equals(session.credential);
	}
	//
	private void rpcRequestCallMessageReceived(RpcSession session,RpcMessage message){
		if(!session.authed){
			logger.error("session need credential:"+session);
			session.write(makeException(
					message.id,
					RpcMessage.TYPE_RPC_CALL_RSP,
					"need credential"));
			session.close();
			return;
		}
		//
		String serviceId=(String)message.payloads[0];
		Object args[]=new Object[message.payloads.length-1];
		if(args.length>0){
			System.arraycopy(message.payloads,1, args, 0, args.length);
		}
		String interfaceClass=serviceId.substring(0,serviceId.indexOf('.'));
		Object instance=instanceMap.get(interfaceClass);
		if(instance==null){
			logger.error("can not find instance:"+interfaceClass);
			session.write(makeException(
					message.id,
					RpcMessage.TYPE_RPC_CALL_RSP,
					"can not find instance:"+interfaceClass));
			return;
		}
		Method method=methodMap.get(serviceId);
		if(method==null){
			logger.error("can not find method:"+serviceId);
			session.write(makeException(
					message.id,
					RpcMessage.TYPE_RPC_CALL_RSP,
					"can not find method:"+serviceId));
			return;
		}
		RPCInvokeCallback callback=new RPCInvokeCallback(session,message);
		Jazmin.dispatcher.invokeInPool(
				"#"+message.id,
				instance, 
				method,
				callback, args);
	}
	//
	static class RPCInvokeCallback extends DispatcherCallbackAdapter{
		private RpcMessage message;
		private RpcSession session;
		public RPCInvokeCallback(RpcSession session,RpcMessage rpcMessage) {
			this.session=session;
			this.message=rpcMessage;
		}
		@Override
		public void before(Object instance, Method method, Object[] args)
				throws Exception {
			rpcSessionThreadLocal.set(session);
		}
		//
		@Override
		public void end(Object instance, Method method, Object[]args,Object ret, Throwable e) {
			rpcSessionThreadLocal.set(null);
			RpcMessage rspMessage=new RpcMessage();
			rspMessage.sentTime=message.sentTime;
			rspMessage.id=message.id;
			rspMessage.type=RpcMessage.TYPE_RPC_CALL_RSP;
			if(e instanceof AppException){
				AppException ae=(AppException)e;
				AppExceptionMessage aem=new AppExceptionMessage();
				aem.code=ae.getCode();
				aem.message=ae.getMessage();
				rspMessage.payloads=new Object[]{ret,aem};
			}else{
				rspMessage.payloads=new Object[]{ret,e};	
			}
			session.write(rspMessage);
		}
	}
	//
	private RpcMessage makeException(int id,int type,String msg){
		RpcMessage rspMessage=new RpcMessage();
		rspMessage.id=id;
		rspMessage.type=type;
		rspMessage.payloads=new Object[]{null,new RpcException(msg)};	
		return rspMessage;
	}
	//
	/**
	 * broadcast message to all session
	 * @param serviceId the service id of message 
	 * @param payload the message
	 */
	public void broadcast(String serviceId,Object payload){
		sessionMap.forEach((name,session)->{
			if(session.disablePushMessage){
				return;
			}
			RpcMessage msg=new RpcMessage();
			msg.type=RpcMessage.TYPE_PUSH;
			msg.payloads=new Object[]{serviceId,payload};
			session.write(msg);
			session.pushPackage();
		});
		addPushMessageCount(serviceId);
	}
	/**
	 * publish message to rpc client which subscribe specified topic id
	 * @param topicId the topic id
	 * @param payload the message 
	 */
	public void publish(String topicId,Object payload){
		List<RpcSession>sessions=topicSessionMap.get(topicId);
		if(sessions==null){
			throw new IllegalArgumentException("can not find topic:"+topicId);
		}
		sessions.forEach(session->{
			if(session.disablePushMessage){
				return;
			}
			RpcMessage msg=new RpcMessage();
			msg.type=RpcMessage.TYPE_PUSH;
			msg.payloads=new Object[]{topicId,payload};
			session.write(msg);
			session.pushPackage();
		});
		//
		addPushMessageCount(topicId);
	}
	//
	private void addPushMessageCount(String id){
		LongAdder count=null;
		if(pushMessageCountMap.containsKey(id)){
			count=pushMessageCountMap.get(id);
		}else{
			count=new LongAdder();
			pushMessageCountMap.put(id, count);
		}
		count.increment();
	}
	//--------------------------------------------------------------------------
	//session
	void sessionCreated(RpcSession session){
		synchronized (session) {
			sessionCreated0(session);
		}
	}
	//
	void sessionCreated0(RpcSession session){
		if(session.principal==null){
			throw new IllegalStateException("session principal can not be null."+session);
		}
		RpcSession oldSession=sessionMap.get(session.getPrincipal());
		if(oldSession!=null){
			logger.warn("kick old rpc session:"+oldSession);
			oldSession.close();
		}
		if(logger.isInfoEnabled()){
			logger.info("rpc session created:"+session+"/topics:"+session.getTopics());
			
		}
		sessionMap.put(session.getPrincipal(),session);
		session.getTopics().forEach((topic)->{
			List<RpcSession>sessions=topicSessionMap.get(topic);
			if(sessions==null){
				sessions=new ArrayList<RpcSession>();
				topicSessionMap.put(topic, sessions);
			}
			sessions.add(session);
		});
	}
	//
	void sessionDestroyed(RpcSession session){
		synchronized (session) {
			sessionDestroyed0(session);
		}
	}
	//
	void sessionDestroyed0(RpcSession session){
		if(session.principal==null){
			return;
		}
		sessionMap.remove(session.getPrincipal());
		session.getTopics().forEach((topic)->{
			List<RpcSession>sessions=topicSessionMap.get(topic);
			if(sessions!=null){
				sessions.remove(session);
			}
		});
	}
	//
	void checkSession(RpcSession session) {
		if(acceptRemoteHosts.isEmpty()){
			return;
		}
		if(!acceptRemoteHosts.contains(session.remoteHostAddress)){
			logger.warn("close session from unaccept remote host {}",
					session.remoteHostAddress);
			session.close();
		}
	}
	/**
	 * add accept remote host addr
	 */
	public void addAcceptRemoteHost(String host){
		acceptRemoteHosts.add(host);
	}
	/**
	 * remove accept remote host
	 * @param host the host will be removed
	 */
	public void removeAcceptRemoteHost(String host){
		acceptRemoteHosts.remove(host);
	}
	/**
	 * return all accept remote hosts
	 * @return all accept remote hosts
	 */
	public List<String>getAcceptRemoteHosts(){
		return new ArrayList<String>(acceptRemoteHosts);
	}
	/**
	 * return credential of this server
	 * @return  credential of this server
	 */
	public String getCredential() {
		return credential;
	}
	/**
	 * set credential of this server
	 * @param credential of this server
	 */
	public void setCredential(String credential) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.credential = credential;
	}
	
	/**
	 * @return the certificateFile
	 */
	public String getCertificateFile() {
		return certificateFile;
	}
	/**
	 * @param certificateFile the certificateFile to set
	 */
	public void setCertificateFile(String certificateFile) {
		this.certificateFile = certificateFile;
	}
	/**
	 * @return the privateKeyFile
	 */
	public String getPrivateKeyFile() {
		return privateKeyFile;
	}
	/**
	 * @param privateKeyFile the privateKeyFile to set
	 */
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}
	/**
	 * @return the privateKeyPhrase
	 */
	public String getPrivateKeyPhrase() {
		return privateKeyPhrase;
	}
	/**
	 * @param privateKeyPhrase the privateKeyPhrase to set
	 */
	public void setPrivateKeyPhrase(String privateKeyPhrase) {
		this.privateKeyPhrase = privateKeyPhrase;
	}
	/**
	 * return port of this server
	 * @return the port of this server
	 */
	public int getPort() {
		return port;
	}
	/**
	 * set port of this server
	 * @param port the port to set
	 */
	public void setPort(int port) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		if(port<0||port>65535){
			throw new IllegalArgumentException("port should >0 and <67735");
		}
		this.port = port;
	}
	
	/**
	 * @return the enableSSL
	 */
	public boolean isEnableSSL() {
		return enableSSL;
	}
	/**
	 * @param enableSSL the enableSSL to set
	 */
	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}
	//
	/**
	 * return idle time of this server
	 * @return the idleTime 
	 */
	public int getIdleTime() {
		return idleTime;
	}
	/**
	 * set idle timeout time of this server
	 * @param idleTime the idleTime to set
	 */
	public void setIdleTime(int idleTime) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.idleTime = idleTime;
	}
	/**
	 * return inbound byte count
	 * @return inbound byte count
	 */
	public long getInBoundBytes(){
		return networkTrafficStat.inBoundBytes.longValue();
	}
	/**
	 * outbound byte count
	 * @return outbound byte count
	 */
	public long getOutBoundBytes(){
		return networkTrafficStat.outBoundBytes.longValue();
	}
	
	/**
	 * return all rpc session 
	 * @return all rpc sessions
	 */
	public List<RpcSession>getSessions(){
		return new ArrayList<RpcSession>(sessionMap.values());
	}
	
	//
	//--------------------------------------------------------------------------
	//lifecycle
	@Override
	public void init() throws Exception {
		initNettyConnector();
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(RpcServerCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		nettyServer.bind(port).sync();
		Jazmin.mointor.registerAgent(new RpcServerMonitorAgent());
	}
	//
	@Override
	public void stop() throws Exception {
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
		}
		if(workerGroup!=null){
			workerGroup.shutdownGracefully();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("port",port)
		.print("credential",credential!=null)
		.print("enableSSL",enableSSL)
		.print("privateKeyFile",getPrivateKeyFile())
		.print("certificateFile",getCertificateFile())
		.print("idleTime",idleTime+" seconds");
		ib.section("accept hosts");
		int index=1;
		List<String>hosts=getAcceptRemoteHosts();
		Collections.sort(hosts);
		for(String s:hosts){
			ib.print(index++,s);
		}
		ib.section("services");
		index=1;
		List<String>methodNames=getServiceNames();
		Collections.sort(methodNames);
		for(String s:methodNames){
			ib.print(index++,s);
		}
		return ib.toString();
	}
	//
	//
	private  class RpcServerMonitorAgent implements MonitorAgent{
		@Override
		public void sample(int idx,Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			info.put("InBoundBytes", getInBoundBytes()+"");
			info.put("OutBoundBytes", getOutBoundBytes()+"");
			monitor.sample("RpcServer.Network",Monitor.CATEGORY_TYPE_COUNT,info);
		}
		//
		@Override
		public void start(Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			info.put("port:",getPort()+"");
			info.put("idleTime:",getIdleTime()+"");
			monitor.sample("RpcServer.Info",Monitor.CATEGORY_TYPE_KV,info);
		}
	}
	
}
