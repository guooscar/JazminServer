/**
 * 
 */
package jazmin.server.msg;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.aop.Dispatcher;
import jazmin.core.aop.DispatcherCallbackAdapter;
import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.console.ConsoleServer;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.amf.AMF3Decoder;
import jazmin.server.msg.codec.amf.AMF3Encoder;
import jazmin.server.msg.codec.json.JSONDecoder;
import jazmin.server.msg.codec.json.JSONEncoder;
import jazmin.server.msg.codec.zjson.ZJSONDecoder;
import jazmin.server.msg.codec.zjson.ZJSONEncoder;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class MessageServer extends Server{
	private static Logger logger=LoggerFactory.get(MessageServer.class);
	//
	public static final String MESSAGE_TYPE_JSON="json";
	public static final String MESSAGE_TYPE_AMF3="amf3";
	public static final String MESSAGE_TYPE_ZJSON="zjson";
	
	//
	static final int DEFAULT_PORT=3001;
	static final int DEFAULT_IDLE_TIME=60*10;//10 min
	static final int DEFAULT_MAX_SESSION_COUNT=8000;
	static final int DEFAULT_MAX_CHANNEL_COUNT=1000;
	static final int DFEAULT_SESSION_TIMEOUT=60*5;//5 min
	//
	ServerBootstrap nettyServer;
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	ChannelInitializer<SocketChannel> channelInitializer;
	int port;
	int idleTime;
	String messageType;
	int sessionTimeout;
	int maxSessionCount;
	int maxChannelCount;
	NetworkTrafficStat networkTrafficStat;
	//
	Map<String,ServiceStub>serviceMap;
	//
	Map<Integer,Session>sessionMap;
	Map<String,Session>principalMap;
	AtomicInteger sessionId;
	Map<String, Channel>channelMap;
	SessionLifecycleListener sessionLifecycleListener;
	Method sessionCreatedMethod;
	Method sessionDisconnectedMethod;
	Method sessionDestroyedMethod;
	//
	ServiceFilter serviceFilter;
	//
	public MessageServer() {
		super();
		serviceMap=new ConcurrentHashMap<String, ServiceStub>();
		sessionMap=new ConcurrentHashMap<>();
		principalMap=new ConcurrentHashMap<>();
		channelMap=new ConcurrentHashMap<>();
		sessionId=new AtomicInteger(1);
		networkTrafficStat=new NetworkTrafficStat();
		messageType=MESSAGE_TYPE_JSON;
		port=DEFAULT_PORT;
		idleTime=DEFAULT_IDLE_TIME;
		maxSessionCount=DEFAULT_MAX_SESSION_COUNT;
		maxChannelCount=DEFAULT_MAX_CHANNEL_COUNT;
		sessionTimeout=DFEAULT_SESSION_TIMEOUT;
		//
		sessionCreatedMethod=Dispatcher.getMethod(
				SessionLifecycleListener.class,
				"sessionCreated",Session.class);
		sessionDisconnectedMethod=Dispatcher.getMethod(
				SessionLifecycleListener.class,
				"sessionDisconnected",Session.class);
		sessionDisconnectedMethod=Dispatcher.getMethod(
				SessionLifecycleListener.class,
				"sessionDestroyed",Session.class);
	}
	/**
	 * @return the port
	 */
	public int port() {
		return port;
	}
	
	/**
	 * @param port the port to set
	 */
	public void port(int port) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.port = port;
	}

	/**
	 * @return the idleTime
	 */
	public int idleTime() {
		return idleTime;
	}

	/**
	 * @param idleTime the idleTime to set
	 */
	public void idleTime(int idleTime) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.idleTime = idleTime;
	}

	/**
	 * @return the messageType
	 */
	public String messageType() {
		return messageType;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void messageType(String messageType) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.messageType = messageType;
	}
	/**
	 * @return the maxSessionCount
	 */
	public int maxSessionCount() {
		return maxSessionCount;
	}

	/**
	 * @param maxSessionCount the maxSessionCount to set
	 */
	public void maxSessionCount(int maxSessionCount) {
		this.maxSessionCount = maxSessionCount;
	}

	/**
	 * @return the maxChannelCount
	 */
	public int maxChannelCount() {
		return maxChannelCount;
	}

	/**
	 * @param maxChannelCount the maxChannelCount to set
	 */
	public void maxChannelCount(int maxChannelCount) {
		this.maxChannelCount = maxChannelCount;
	}
	//
	public List<String>serviceNames(){
		return new ArrayList<String>(serviceMap.keySet());
	}
	//
	List<ServiceStub>services(){
		return new ArrayList<ServiceStub>(serviceMap.values());
	}
	//
	public List<Session>sessions(){
		return new ArrayList<Session>(sessionMap.values());
	}
	//
	public int sessionCount(){
		return sessionMap.size();
	}
	//
	public List<Channel>channels(){
		return new ArrayList<Channel>(channelMap.values());
	}
	//
	public long inBoundBytes(){
		return networkTrafficStat.inBoundBytes.longValue();
	}
	//
	public long outBoundBytes(){
		return networkTrafficStat.outBoundBytes.longValue();
	}
	//--------------------------------------------------------------------------
	//
	private void startSessionChecker(){
		Jazmin.scheduleAtFixedRate(()->{
			long currentTime = System.currentTimeMillis();
			for(Session session:sessionMap.values()){
				try{
					checkPrincipal(currentTime,session);	
					checkSessionTimeout(currentTime, session);
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
			}
		},30,30,TimeUnit.SECONDS);
	}
	/*
	 *if session didn't set principal over 30 seconds,kick it.
	 */
	private void checkPrincipal(long currentTime,Session session) {
		if (session.principal() == null) {
			if ((currentTime - session.createTime.getTime())> 30 * 1000) {
				session.kick("principal not set.");
			}
		}
	}
	/*
	 * if session is not active,kick it after session timeout reached.
	 */
	private void checkSessionTimeout(long currentTime,Session session){
		if(!session.isActive()){
			if((currentTime-session.lastAccessTime())>sessionTimeout*1000){
				sessionDestroyed(session);
			}
		}
	}
	/**
	 * set global session lifecycle listener.
	 */
	public void sessionLifecycleListener(SessionLifecycleListener l){
		this.sessionLifecycleListener=l;
	}
	//
	public SessionLifecycleListener sessionLifecycleListener(){
		return sessionLifecycleListener;
	}
	/**
	 * set global service filter.
	 */
	public void serviceFilter(ServiceFilter sf){
		this.serviceFilter=sf;
	}
	//
	public ServiceFilter serviceFilter(){
		return serviceFilter;
	}
	//--------------------------------------------------------------------------
	class MessageServerChannelInitializer extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(idleTime,idleTime,0));
			if(messageType.equals(MESSAGE_TYPE_JSON)){
				ch.pipeline().addLast(
						new JSONEncoder(networkTrafficStat),
						new JSONDecoder(networkTrafficStat),
						new MessageServerHandler(MessageServer.this));
			}
			if(messageType.equals(MESSAGE_TYPE_AMF3)){
				ch.pipeline().addLast(
						new AMF3Encoder(networkTrafficStat),
						new AMF3Decoder(networkTrafficStat),
						new MessageServerHandler(MessageServer.this));
			}
			if(messageType.equals(MESSAGE_TYPE_ZJSON)){
				ch.pipeline().addLast(
						new ZJSONEncoder(networkTrafficStat),
						new ZJSONDecoder(networkTrafficStat),
						new MessageServerHandler(MessageServer.this));
			}
			
		}
	}
	//
	protected void initNettyServer(){
		nettyServer=new ServerBootstrap();
		channelInitializer=new MessageServerChannelInitializer();
		
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 128)    
		.option(ChannelOption.SO_REUSEADDR, true)    
		.childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true) 
		.childHandler(channelInitializer);
	}
	//
	//--------------------------------------------------------------------------
	//
	private boolean checkParameterTypes(Method m){
		if(!Modifier.isPublic(m.getModifiers())){
			return false;
		}
		/*first parameter type must be Context*/
		Class<?>[]pTypes=m.getParameterTypes();
		if(pTypes.length==0||!pTypes[0].equals(Context.class)){
			return false;
		}
		for(int i=1;i<pTypes.length;i++){
			Class<?>type=pTypes[i];
			if(!type.equals(String.class)&&
					!type.equals(Short.class)&&
					!type.equals(Integer.class)&&
					!type.equals(Double.class)&&
					!type.equals(Float.class)&&
					!type.equals(Long.class)&&
					!type.equals(Boolean.class)){
				logger.warn("{}.{}/parameter must be String/Integer/"
						+ "Double/Float/Long/Boolean/Short",
						m.getDeclaringClass().getSimpleName(),
						m.getName());
				return false;
			}
		}
		return true;
	}
	/**
	 * register service to message server
	 */
	public void registerService(Object instance){
		Class<?>interfaceClass=instance.getClass();
		for(Method m:interfaceClass.getDeclaredMethods()){
			if(!checkParameterTypes(m)){
				continue;
			}
			String methodName=interfaceClass.getSimpleName()+"."+m.getName();
			//
			if(serviceMap.containsKey(methodName)){
				throw new IllegalArgumentException(
					"service:"+methodName+" already exists.");
			}
			ServiceStub ss=new ServiceStub();
			ss.serviceId=methodName;
			ss.isAsyncService=m.isAnnotationPresent(ServiceAsync.class);
			ss.isContinuationService=m.isAnnotationPresent(ServiceContinuation.class);
			ss.isDisableResponseService=m.isAnnotationPresent(ServiceDisableResponse.class);
			ss.instance=instance;
			ss.method=m;
			serviceMap.put(methodName, ss);
		}
	}
	//
	//--------------------------------------------------------------------------
	//
	private ServiceStub checkMessage(Session session,RequestMessage message){
		session.lastAccess();
		//1.bad message
		if(message.isBadRequest||message.requestId<=0){
			session.sendError(
					message,ResponseMessage.SC_BAD_MESSAGE,"bad message");
			return null;
		}
		//2.replay attack
		if(message.requestId<=session.getRequestId()){
			session.sendError(
					message,ResponseMessage.SC_REPEAT_ATTACK,
					"same request id:"+message.requestId);
			return null;
		}
		session.receivedMessage(message);
		//3.request rate check
		if(session.isFrequencyReach()){
			session.sendError(
					message,ResponseMessage.SC_REQUEST_RATE_TOO_HIGH,
					"request rate too high");
			return null;
		}
		//4.get service 
		ServiceStub ss=serviceMap.get(message.serviceId);
		if(ss==null){
			session.sendError(
					message,ResponseMessage.SC_BAD_MESSAGE,
					"can not find serviceId:"+message.serviceId);
			return null;
		}
		//5.check async state
		if(!ss.isAsyncService&&session.isProcessSyncService()){
			session.sendError(
					message,ResponseMessage.SC_SYNC_SERVICE,
					"sync service processing");
			return null;
		}
		return ss;
	}
	//
	private Object []getParameters(
			Context context,
			Session session,
			ServiceStub stub,
			RequestMessage message){
		Class<?> types[]=stub.method.getParameterTypes();
		Object []parameters=new Object[types.length];
		parameters[0]=context;
		for(int i=1;i<types.length;i++){
			Class<?>type=types[i];
			int pIdx=i-1;
			if(type.equals(String.class)){
				parameters[i]=context.getString(pIdx);
			}else if(type.equals(Integer.class)){
				parameters[i]=context.getInteger(pIdx);
			}else if(type.equals(Boolean.class)){
				parameters[i]=context.getBoolean(pIdx);
			}else if(type.equals(Short.class)){
				parameters[i]=context.getShort(pIdx);
			}else if(type.equals(Double.class)){
				parameters[i]=context.getDouble(pIdx);
			}else if(type.equals(Long.class)){
				parameters[i]=context.getLong(pIdx);
			}else if(type.equals(Float.class)){
				parameters[i]=context.getFloat(pIdx);
			}
			//
			if(parameters[i]==null){
				session.sendError(
						message,
						ResponseMessage.SC_ILLEGA_ARGUMENT, 
						"parameter:"+pIdx+"["+types[i].getSimpleName()+"] required");
				return null;
			}
		}
		return parameters;
	}
	//
	private void invokeService(Session session,ServiceStub stub,RequestMessage message){
		Context context=new Context(
				this,
				session,
				message,
				stub.isDisableResponseService,
				stub.isContinuationService);
		Object []parameters;
		try {
			parameters=getParameters(context, session, stub, message);
		} catch (Exception e) {
			session.sendError(
					message,
					ResponseMessage.SC_ILLEGA_ARGUMENT, 
					"parameter error:"+e.getMessage());
			return;
		}
		if(parameters==null){
			return;
		}
		//mark async 
		session.processSyncService(!stub.isAsyncService);
		//
		MessageDispatcherCallback callback=new MessageDispatcherCallback();
		callback.session=session;
		callback.requestMessage=message;
		callback.serviceFilter=serviceFilter;
		Jazmin.dispatcher.invokeInPool(
				"@"+session.principal+"#"+message.requestId,
				stub.instance,
				stub.method, 
				callback,parameters);
	}
	//
	static class MessageDispatcherCallback extends DispatcherCallbackAdapter{
		public Session session;
		public RequestMessage requestMessage;
		public ServiceFilter serviceFilter;
		//
		@Override
		public void before(Object instance, Method method, Object[] args)throws Exception {
			if(serviceFilter!=null){
				serviceFilter.before((Context) args[0]);
			}
		}
		//
		@Override
		public void end(Object instance, Method method, Object[]args,Object ret,Throwable e) {
			session.processSyncService(false);
			if(serviceFilter!=null){
				try{
					serviceFilter.after((Context) args[0], e);
				}catch(Exception ee){
					logger.error(e.getMessage(),e);
				}
			}
			Context context=(Context)args[0];
			if(e!=null){
				if(e instanceof AppException){
					AppException ae=(AppException)e;
					session.sendError(
							requestMessage,
							ResponseMessage.SC_APP_EXCEPTION, 
							ae.getCode()+"."+ae.getMessage());					
				}else{
					session.sendError(
							requestMessage,
							ResponseMessage.SC_SYSTEM_EXCEPTION, 
							e.getMessage());			
				}	
			}else{
				context.close();		
			}
		}	
	}
	//message
	void receiveMessage(Session session,RequestMessage message){
		ServiceStub ss=checkMessage(session, message);
		if(ss==null){
			return;
		}
		invokeService(session, ss, message);
	}
	//
	//--------------------------------------------------------------------------
	//session
	void sessionIdle(Session session){
		session.kick("user idle 10 minutes,last access:"+
				new Date(session.lastAccessTime));
	}
	/*
	 */
	void sessionCreated(Session session){
		synchronized (session) {
			sessionCreated0(session);
		}
	}
	//
	private void sessionCreated0(Session session){
		session.setActive(true);
		if(sessionMap.size()>=maxSessionCount){
			session.kick("too many sessions:"+maxSessionCount);
			return;
		}
		int nextSessionId=sessionId.incrementAndGet();
		session.setId(nextSessionId);
		if(logger.isDebugEnabled()){
			logger.debug("session created:"+
					session.id+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
		}
		//
		sessionMap.put(session.id,session);
		//fire session create event in thread pool
		if(sessionLifecycleListener!=null){
			Jazmin.dispatcher.invokeInPool(
					session.principal,
					sessionLifecycleListener,
					sessionCreatedMethod,
					Dispatcher.EMPTY_CALLBACK,
					session);
		}
	}
	/*
	 */
	void sessionDisconnected(Session session){
		synchronized (session) {
			sessionDisconnected0(session);
		}
	}
	/**
	 * 
	 * @param session
	 */
	private void sessionDisconnected0(Session session){
		if(session.principal==null){
			logger.debug("bad session disconnected:"+
					session.id+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
			sessionMap.remove(session.id);
			return;
		}
		session.setActive(false);
		session.lastAccess();
		if(logger.isDebugEnabled()){
			logger.debug("session disconnected:"+
					session.id+"/"+session.principal+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
		}
		//fire session disconnect event in thread pool
		if(sessionLifecycleListener!=null){
			Jazmin.dispatcher.invokeInPool(
					session.principal,
					sessionLifecycleListener,
					sessionDisconnectedMethod,
					Dispatcher.EMPTY_CALLBACK,
					session);
		}
	}
	//
	void sessionDestroyed(Session session){
		synchronized (session) {
			sessionDestroyed0(session);
		}
	}
	//
	private void sessionDestroyed0(Session session){
		if(logger.isDebugEnabled()){
			logger.debug("session destroyed:"+
					session.id+"/"+session.principal+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
		}
		sessionMap.remove(session.id);
		//
		if(session.principal!=null){
			principalMap.remove(session.principal);
		}
		//auto remove disconnect session from room
		session.channels.forEach(cname->{
			Channel cc=channel(cname);
			if(cc!=null){
				session.leaveChannel(cc);
			}
		});
		//fire session disconnect event in thread pool
		if(sessionLifecycleListener!=null){
			Jazmin.dispatcher.invokeInPool(
					session.principal,
					sessionLifecycleListener,
					sessionDestroyedMethod,
					Dispatcher.EMPTY_CALLBACK,
					session);
		}
	}
	/**
	 * get session by principal
	 */
	public Session getSessionByPrincipal(String principal){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		return principalMap.get(principal);
	}
	/**
	 * get session by id
	 */
	public Session getSessionById(int id){
		return sessionMap.get(id);
	}
	/**
	 * setup session principal info,user agent is an option but recommend
	 */
	public void principal(Session s,String principal,String userAgent){
		synchronized (s) {
			setPrincipal0(s,principal,userAgent);
		}
	}
	//
	private void setPrincipal0(Session session,String principal,String userAgent){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		if(session.principal!=null){
			throw new IllegalStateException("principal already set to:"+
					session.principal);
		}
		session.setPrincipal(principal);
		session.setUserAgent(userAgent);
		Session oldSession=principalMap.get(principal);
		if(oldSession==session){
			//same session,ignore it
			return ;
		}
		if(oldSession!=null){
			principalMap.remove(principal);
			sessionMap.remove(oldSession.id);
			if(oldSession.isActive()){
				oldSession.setPrincipal(null);
				oldSession.kick("kicked by same principal.");		
			}
			//同名用户登录成功以后新session使用老用户的缓存信息
			session.userObject(oldSession.userObject);
		}
		//NOTE 在old session存在的情况下，新的session会直接替换掉老的session 老的session
		//不会触发sessionDestroyed事件，这么做的目的是防止出现新用户登录提到同名老用户
		//业务层收到sessionDestroyed事件认为用户下线，造成困扰
		//
		principalMap.put(principal,session);
	}
	//--------------------------------------------------------------------------
	/**
	 * board message to all session
	 */
	public void broadcast(String serviceId,Object payload){
		sessionMap.forEach((id,session)->{
			if(session.isActive()){
				session.push(serviceId, payload);
			}
		});
	}
	//
	
	//--------------------------------------------------------------------------
	public Channel createChannel(String id){
		if(logger.isDebugEnabled()){
			logger.debug("create channel:"+id);
		}
		if(id==null){
			throw new IllegalArgumentException("id can not be null.");
		}
		Channel channel=channelMap.get(id);
		if(channel!=null){
			return channel;
		}
		if(channelMap.size()>maxChannelCount){
			throw new IllegalStateException("too many channel,max:"+maxChannelCount);
		}
		channel=new Channel(this,id);
		channelMap.put(id, channel);
		return channel;
	}
	/**
	 * 
	 */
	public Channel channel(String id){
		return channelMap.get(id);
	}
	/**
	 *return total channel count 
	 */
	public int channelCount(){
		return channelMap.size();
	}
	/**
	 * 
	 */
	void removeChannelInternal(String id){
		if(logger.isDebugEnabled()){
			logger.debug("remove channel:"+id);
		}
		channelMap.remove(id);
	}
	//--------------------------------------------------------------------------
	//lifecycle
	@Override
	public void init() throws Exception {
		initNettyServer();
		ConsoleServer cs=Jazmin.server(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new MessageServerCommand());
		}
	}
	//
	@Override
	public void start() throws Exception {
		nettyServer.bind(port).sync();
		startSessionChecker();
	}
	//
	@Override
	public void stop() throws Exception {
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib= InfoBuilder.create()
		.section("info")
		.format("%-50s:%-30s\n")
		.print("port", port)
		.print("idleTime", idleTime+" seconds")
		.print("messageType", messageType)
		.print("sessionTimeout", sessionTimeout+" seconds")
		.print("maxSessionCount", maxSessionCount)
		.print("maxChannelCount", maxChannelCount)
		.print("sessionLifecycleListener", sessionLifecycleListener)
		.print("serviceFilter", serviceFilter);
		ib.section("services");
		List<ServiceStub>ss=new ArrayList<ServiceStub>(serviceMap.values());
		Collections.sort(ss);
		ss.forEach((stub)->{
			ib.print(stub.serviceId, stub);
		});			
		return ib.toString();
	}
	
}
