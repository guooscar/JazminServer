/**
 * 
 */
package jazmin.server.im;

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
import jazmin.core.app.AppException;
import jazmin.core.thread.Dispatcher;
import jazmin.core.thread.DispatcherCallbackAdapter;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.console.ConsoleServer;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class IMMessageServer extends Server{
	private static Logger logger=LoggerFactory.get(IMMessageServer.class);
	static final int DEFAULT_PORT=5001;
	static final int DEFAULT_IDLE_TIME=60*10;//10 min
	static final int DEFAULT_MAX_SESSION_COUNT=8000;
	static final int DEFAULT_MAX_CHANNEL_COUNT=1000;
	//
	ServerBootstrap nettyServer;
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	ChannelInitializer<SocketChannel> channelInitializer;
	int port;
	int idleTime;
	int maxSessionCount;
	int maxChannelCount;
	int maxSessionRequestCountPerSecond;
	NetworkTrafficStat networkTrafficStat;
	//
	Map<Integer,IMServiceStub>serviceMap;
	//
	Map<Integer,IMSession>sessionMap;
	Map<String,IMSession>principalMap;
	AtomicInteger sessionId;
	Map<String, IMChannel>channelMap;
	IMSessionLifecycleListener sessionLifecycleListener;
	Method sessionCreatedMethod;
	Method sessionDisconnectedMethod;
	//
	IMServiceFilter serviceFilter;
	//
	public IMMessageServer() {
		super();
		serviceMap=new ConcurrentHashMap<Integer, IMServiceStub>();
		sessionMap=new ConcurrentHashMap<>();
		principalMap=new ConcurrentHashMap<>();
		channelMap=new ConcurrentHashMap<>();
		sessionId=new AtomicInteger(1);
		networkTrafficStat=new NetworkTrafficStat();
		port=DEFAULT_PORT;
		idleTime=DEFAULT_IDLE_TIME;
		maxSessionCount=DEFAULT_MAX_SESSION_COUNT;
		maxChannelCount=DEFAULT_MAX_CHANNEL_COUNT;
		//
		sessionCreatedMethod=Dispatcher.getMethod(
				IMSessionLifecycleListener.class,
				"sessionCreated",IMSession.class);
		sessionDisconnectedMethod=Dispatcher.getMethod(
				IMSessionLifecycleListener.class,
				"sessionDisconnected",IMSession.class);
		maxSessionRequestCountPerSecond=10;
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
		checkServerState();
		this.port = port;
	}

	/**
	 * @return the idleTime
	 */
	public int getIdleTime() {
		return idleTime;
	}

	/**
	 * @param idleTime the idleTime to set
	 */
	public void setIdleTime(int idleTime) {
		checkServerState();
		this.idleTime = idleTime;
	}

	/**
	 * @return the maxSessionRequestCountPerSecond
	 */
	public int getMaxSessionRequestCountPerSecond() {
		return maxSessionRequestCountPerSecond;
	}
	/**
	 * @param maxSessionRequestCountPerSecond the maxSessionRequestCountPerSecond to set
	 */
	public void setMaxSessionRequestCountPerSecond(
			int maxSessionRequestCountPerSecond) {
		this.maxSessionRequestCountPerSecond = maxSessionRequestCountPerSecond;
	}
	/**
	 * @return the maxSessionCount
	 */
	public int getMaxSessionCount() {
		return maxSessionCount;
	}

	/**
	 * @param maxSessionCount the maxSessionCount to set
	 */
	public void setMaxSessionCount(int maxSessionCount) {
		this.maxSessionCount = maxSessionCount;
	}

	/**
	 * @return the maxChannelCount
	 */
	public int getMaxChannelCount() {
		return maxChannelCount;
	}

	/**
	 * @param maxChannelCount the maxChannelCount to set
	 */
	public void setMaxChannelCount(int maxChannelCount) {
		this.maxChannelCount = maxChannelCount;
	}
	//
	private void checkServerState(){
		if(isStarted()){
			throw new IllegalStateException("set before started");
		}
	}
	//
	public List<Integer>getServiceNames(){
		return new ArrayList<Integer>(serviceMap.keySet());
	}
	//
	List<IMServiceStub>getServices(){
		return new ArrayList<IMServiceStub>(serviceMap.values());
	}
	//
	public List<IMSession>getSessions(){
		return new ArrayList<IMSession>(sessionMap.values());
	}
	//
	public int getSessionCount(){
		return sessionMap.size();
	}
	//
	public List<IMChannel>getChannels(){
		return new ArrayList<IMChannel>(channelMap.values());
	}
	//
	public long getInBoundBytes(){
		return networkTrafficStat.inBoundBytes.longValue();
	}
	//
	public long getOutBoundBytes(){
		return networkTrafficStat.outBoundBytes.longValue();
	}
	//--------------------------------------------------------------------------
	//
	private void startSessionChecker(){
		Jazmin.scheduleAtFixedRate(()->{
			long currentTime = System.currentTimeMillis();
			for(IMSession session:sessionMap.values()){
				try{
					checkPrincipal(currentTime,session);	
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
			}
		},30,30,TimeUnit.SECONDS);
	}
	/*
	 *if session didn't set principal over 30 seconds,kick it.
	 */
	private void checkPrincipal(long currentTime,IMSession session) {
		if (session.getPrincipal() == null) {
			if ((currentTime - session.createTime.getTime())> 30 * 1000) {
				session.kick("principal not set.");
			}
		}
	}
	/**
	 * set global session lifecycle listener.
	 */
	public void setSessionLifecycleListener(IMSessionLifecycleListener l){
		this.sessionLifecycleListener=l;
	}
	//
	public IMSessionLifecycleListener getSessionLifecycleListener(){
		return sessionLifecycleListener;
	}
	/**
	 * set global service filter.
	 */
	public void setServiceFilter(IMServiceFilter sf){
		this.serviceFilter=sf;
	}
	//
	public IMServiceFilter getServiceFilter(){
		return serviceFilter;
	}
	//--------------------------------------------------------------------------
	class MessageServerChannelInitializer extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(idleTime,idleTime,0));
				ch.pipeline().addLast(
						new IMEncoder(networkTrafficStat),
						new IMDecoder(networkTrafficStat),
						new IMMessageServerHandler(IMMessageServer.this));
		}
	}
	//
	protected void initNettyServer(){
		nettyServer=new ServerBootstrap();
		channelInitializer=new MessageServerChannelInitializer();
		IOWorker worker=new IOWorker("MsgServerIO",Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup = new NioEventLoopGroup(1,worker);
		workerGroup = new NioEventLoopGroup(0,worker);
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)    
		.option(ChannelOption.SO_REUSEADDR, true)   
		.option(ChannelOption.SO_RCVBUF, 1024*256)   
		.option(ChannelOption.SO_SNDBUF, 1024*256)   
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
		if(pTypes.length!=2){
			return false;
		}
		if(!pTypes[0].equals(IMContext.class)||!pTypes[1].equals(byte[].class)){
			return false;
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
				logger.warn("bad parameter types on method:{}",m.getName());
				continue;
			}
			IMService serviceAnno=m.getAnnotation(IMService.class);
			if(serviceAnno==null){
				logger.warn("can not found Annotation:IMService on method:{}",m.getName());
				continue;
			}
			//
			if(serviceMap.containsKey(serviceAnno.id())){
				throw new IllegalArgumentException(
					"service:"+serviceAnno.id()+" already exists.");
			}
			IMServiceStub ss=new IMServiceStub();
			ss.serviceId=serviceAnno.id();
			ss.isSyncOnSessionService=serviceAnno.syncOnSession();
			ss.isRestrictRequestRate=serviceAnno.restrictRequestRate();
			ss.isContinuationService=serviceAnno.continuation();
			ss.instance=instance;
			ss.method=m;
			serviceMap.put(serviceAnno.id(), ss);
		}
	}
	//
	//--------------------------------------------------------------------------
	//
	private IMServiceStub checkMessage(IMSession session,IMRequestMessage message){
		session.lastAccess();
		session.receivedMessage(message);
		//4.get service 
		IMServiceStub ss=serviceMap.get(message.serviceId);
		if(ss==null){
			session.sendError(
					message,ResponseMessage.SC_BAD_MESSAGE,
					"can not find serviceId:"+message.serviceId);
			return null;
		}
		//3.request rate check
		if(ss.isRestrictRequestRate&&session.isFrequencyReach()){
			if(logger.isWarnEnabled()){
				logger.warn("request rate too high");
			}
			session.sendError(
					message,ResponseMessage.SC_REQUEST_RATE_TOO_HIGH,
					"request rate too high");
			return null;
		}
		
		//5.check async state
		if(ss.isSyncOnSessionService&&session.isProcessSyncService()){
			if(logger.isWarnEnabled()){
				logger.warn("processing sync service:"+ss.serviceId);
			}
			session.sendError(
					message,ResponseMessage.SC_SYNC_SERVICE,
					"sync service processing");
			return null;
		}
		return ss;
	}
	
	//
	private void invokeService(IMSession session,IMServiceStub stub,IMRequestMessage message){
		IMContext context=new IMContext(
				this,
				session,
				message,
				stub.isContinuationService);
		Object []parameters=new Object[]{context,message.rawData};
		
		//mark async 
		session.processSyncService(stub.isSyncOnSessionService);
		//
		MessageDispatcherCallback callback=new MessageDispatcherCallback();
		callback.session=session;
		callback.requestMessage=message;
		callback.serviceFilter=serviceFilter;
		Jazmin.dispatcher.invokeInPool(
				"@"+session.principal+"#"+message.serviceId,
				stub.instance,
				stub.method, 
				callback,parameters);
	}
	//
	static class MessageDispatcherCallback extends DispatcherCallbackAdapter{
		public IMSession session;
		public IMRequestMessage requestMessage;
		public IMServiceFilter serviceFilter;
		//
		@Override
		public void before(Object instance, Method method, Object[] args)throws Exception {
			if(serviceFilter!=null){
				serviceFilter.before((IMContext) args[0]);
			}
		}
		//
		@Override
		public void end(Object instance, Method method, Object[]args,Object ret,Throwable e) {
			session.processSyncService(false);
			if(serviceFilter!=null){
				try{
					serviceFilter.after((IMContext) args[0], e);
				}catch(Exception ee){
					logger.error(e.getMessage(),e);
				}
			}
			IMContext context=(IMContext)args[0];
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
	void receiveMessage(IMSession session,IMRequestMessage message){
		IMServiceStub ss=checkMessage(session, message);
		if(ss==null){
			logger.warn("can not execute service stub:0x{}",Integer.toHexString(message.serviceId));
			return;
		}
		invokeService(session, ss, message);
	}
	//
	//--------------------------------------------------------------------------
	//session
	void sessionIdle(IMSession session){
		session.kick("user idle 10 minutes,last access:"+
				new Date(session.lastAccessTime));
	}
	/*
	 */
	void sessionCreated(IMSession session){
		synchronized (session) {
			sessionCreated0(session);
		}
	}
	//
	private void sessionCreated0(IMSession session){
		session.setMaxRequestCountPerSecond(maxSessionRequestCountPerSecond);
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
	void sessionDisconnected(IMSession session){
		synchronized (session) {
			sessionDisconnected0(session);
		}
	}
	/**
	 * 
	 * @param session
	 */
	private void sessionDisconnected0(IMSession session){
		if(session.principal==null){
			logger.debug("bad session disconnected:"+
					session.id+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
			sessionMap.remove(session.id);
			return;
		}
		if(logger.isDebugEnabled()){
			logger.debug("session disconnected:"+
					session.id+"/"+session.principal+"/"+
					session.remoteHostAddress+":"+
					session.remotePort);
		}
		sessionMap.remove(session.id);
		//
		if(session.principal!=null){
			principalMap.remove(session.principal);
		}
		//
		//auto remove disconnect session from room
		session.channels.forEach(cname->{
			IMChannel cc=getChannel(cname);
			if(cc!=null){
				if(cc.isAutoRemoveDisconnectedSession()){
					cc.removeSession(session);
				}
			}
		});
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
	/**
	 * get session by principal
	 */
	public IMSession getSessionByPrincipal(String principal){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		return principalMap.get(principal);
	}
	/**
	 * get session by id
	 */
	public IMSession getSessionById(int id){
		return sessionMap.get(id);
	}
	/**
	 * setup session principal info,user agent is an option but recommend
	 */
	public void setPrincipal(IMSession s,String principal,String userAgent){
		synchronized (s) {
			setPrincipal0(s,principal,userAgent);
		}
	}
	//
	private void setPrincipal0(IMSession session,String principal,String userAgent){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		if(session.principal!=null){
			throw new IllegalStateException("principal already set to:"+
					session.principal);
		}
		session.setPrincipal(principal);
		session.setUserAgent(userAgent);
		IMSession oldSession=principalMap.get(principal);
		if(oldSession==session){
			//same session,ignore it
			return ;
		}
		if(oldSession!=null){
			principalMap.remove(principal);
			sessionMap.remove(oldSession.id);
			oldSession.setPrincipal(null);
			oldSession.kick("kicked by same principal.");		
			session.setUserObject(oldSession.userObject);
		}
		//
		principalMap.put(principal,session);
	}
	//--------------------------------------------------------------------------
	/**
	 * board message to all session
	 */
	public void broadcast(byte []bb){
		sessionMap.forEach((id,session)->{
			session.push(bb);
		});
	}
	//
	
	//--------------------------------------------------------------------------
	public IMChannel createChannel(String id){
		if(logger.isDebugEnabled()){
			logger.debug("create channel:"+id);
		}
		if(id==null){
			throw new IllegalArgumentException("id can not be null.");
		}
		IMChannel channel=channelMap.get(id);
		if(channel!=null){
			return channel;
		}
		if(channelMap.size()>maxChannelCount){
			throw new IllegalStateException("too many channel,max:"+maxChannelCount);
		}
		channel=new IMChannel(this,id);
		channelMap.put(id, channel);
		return channel;
	}
	/**
	 * 
	 */
	public IMChannel getChannel(String id){
		return channelMap.get(id);
	}
	/**
	 *return total channel count 
	 */
	public int getChannelCount(){
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
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(IMMessageServerCommand.class);
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
		setSessionLifecycleListener(null);
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
		.print("maxSessionCount", maxSessionCount)
		.print("maxChannelCount", maxChannelCount)
		.print("sessionLifecycleListener", sessionLifecycleListener)
		.print("serviceFilter", serviceFilter);
		ib.section("services");
		List<IMServiceStub>ss=new ArrayList<IMServiceStub>(serviceMap.values());
		Collections.sort(ss);
		ss.forEach((stub)->{
			ib.print("0x"+Integer.toHexString(stub.serviceId), stub);
		});			
		return ib.toString();
	}
	
}
