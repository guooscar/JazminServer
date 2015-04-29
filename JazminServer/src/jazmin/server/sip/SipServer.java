/**
 * 
 */
package jazmin.server.sip;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Jazmin;
import jazmin.core.JazminThreadFactory;
import jazmin.core.Server;
import jazmin.core.aop.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ToHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader.ViaHeaderBuilder;
import jazmin.server.sip.stack.Connection;
import jazmin.server.sip.stack.SipMessageDatagramDecoder;
import jazmin.server.sip.stack.SipMessageEncoder;
import jazmin.server.sip.stack.SipMessageStreamDecoder;

/**
 * @author yama
 *
 */
public class SipServer extends Server{
	private static Logger logger=LoggerFactory.get(SipServer.class);
	//
	private String address;
    private int port;
	private String publicAddress;
	private int publicPort;
	private int sessionTimeout;
	//
	private int webSocketPort;
    static final int MIN_SESSION_TIMEOUT=60;
    private  EventLoopGroup bossGroup;
    private  EventLoopGroup workerGroup;
    private  EventLoopGroup udpGroup;
    private  SipSocketHandler handler;
    private  ServerBootstrap tcpServerBootstrap;
    private  ServerBootstrap webSocketServerBootstrap;
    private  Bootstrap bootstrap;
    private  Channel udpListeningPoint = null;
    private  SipMessageHandler messageHandler;
    private  Method handlerMethod;
    private  Map<String,SipSession>sessionMap;
    private  LongAdder sessionIdLongAdder;
    private  Map<SipURI, SipLocationBinding> locationStore;
    private  Map<String, SipChannel>channels;
    //
    private ScheduledExecutorService scheduledExecutorService;
    //
    public SipServer() {
        this.address = "0.0.0.0";
        this.publicAddress="127.0.0.1";
        this.publicPort=5060;
        this.port = 5060;
        this.webSocketPort=1443;
        handlerMethod=Dispatcher.getMethod(
        		SipServer.class,"handleMessage",
        		SipContext.class);
        sessionMap=new ConcurrentHashMap<String, SipSession>();
        locationStore=new ConcurrentHashMap<SipURI, SipLocationBinding>();
        channels=new ConcurrentHashMap<String, SipChannel>();
        sessionIdLongAdder=new LongAdder();
        sessionTimeout=60;
        scheduledExecutorService=new ScheduledThreadPoolExecutor(
				3,
				new JazminThreadFactory("SipScheduledExecutor"),
				new ThreadPoolExecutor.AbortPolicy());
    }
   
    //
    private void startNetty() throws Exception {
    	final InetSocketAddress socketAddress = new InetSocketAddress(this.address, this.port);
    	final InetSocketAddress wsSocketAddress = new InetSocketAddress(this.address, this.webSocketPort);
    	this.udpListeningPoint = this.bootstrap.bind(socketAddress).sync().channel();
        this.tcpServerBootstrap.bind(socketAddress).sync();
        this.webSocketServerBootstrap.bind(wsSocketAddress).sync();
    }
    //
    private Bootstrap createUDPListeningPoint() {
        final Bootstrap b = new Bootstrap();
        b.group(this.udpGroup)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(final DatagramChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new SipMessageDatagramDecoder());
                pipeline.addLast("encoder", new SipMessageEncoder());
                pipeline.addLast("handler", handler);
            }
        });
        return b;
    }
    //
    private ServerBootstrap createTCPListeningPoint() {
        final ServerBootstrap b = new ServerBootstrap();

        b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new SipMessageStreamDecoder());
                pipeline.addLast("encoder", new SipMessageEncoder());
                pipeline.addLast("handler", handler);
            }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true);
        return b;
    }
    //
    private ServerBootstrap createWSListeningPoint() {
        final ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(3600,3600,0));
    			ch.pipeline().addLast(new HttpServerCodec());
    			ch.pipeline().addLast(new HttpObjectAggregator(65536));
    			ch.pipeline().addLast(new WebSocketServerCompressionHandler());
    			ch.pipeline().addLast(new SipWebSocketHandler(SipServer.this));
                pipeline.addLast("handler", handler);
            }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true);
        return b;
    }
    //-------------------------------------------------------------------------
    /**
	 * @return the ip
	 */
	public String getHostAddress() {
		return address;
	}


	/**
	 * @return the publicAddress
	 */
	public String getPublicAddress() {
		return publicAddress;
	}

	/**
	 * @param publicAddress the publicAddress to set
	 */
	public void setPublicAddress(String publicAddress) {
		this.publicAddress = publicAddress;
	}

	/**
	 * @return the publicPort
	 */
	public int getPublicPort() {
		return publicPort;
	}

	/**
	 * @param publicPort the publicPort to set
	 */
	public void setPublicPort(int publicPort) {
		this.publicPort = publicPort;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setHostAddress(String ip) {
		if(isInited()){
			throw new IllegalStateException("set before inited");
		}
		this.address = ip;
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
		if(isInited()){
			throw new IllegalStateException("set before inited");
		}
		this.port = port;
	}

	/**
	 * @return the sessionTimeout
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	
	
	/**
	 * @return the webSocketPort
	 */
	public int getWebSocketPort() {
		return webSocketPort;
	}

	/**
	 * @param webSocketPort the webSocketPort to set
	 */
	public void setWebSocketPort(int webSocketPort) {
		this.webSocketPort = webSocketPort;
	}

	/**
	 * @param sessionTimeout the sessionTimeout to set
	 */
	public void setSessionTimeout(int sessionTimeout) {
		if(isInited()){
			throw new IllegalStateException("set before inited");
		}
		if(sessionTimeout<MIN_SESSION_TIMEOUT){
			throw new IllegalArgumentException("session timeout should > "
					+MIN_SESSION_TIMEOUT);
		}
		this.sessionTimeout = sessionTimeout;
	}
	/**
	 * @return the messageHandler
	 */
	public SipMessageHandler getMessageHandler() {
		return messageHandler;
	}
	/**
	 * @param messageHandler the messageHandler to set
	 */
	public void setMessageHandler(SipMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	//
	public List<SipChannel>getChannels(){
		return new ArrayList<SipChannel>(channels.values());
	}
	//
	//
	String getServerHost(){
		String serverHost=getHostAddress();
		if(publicAddress!=null){
			serverHost=publicAddress;
		}
		return serverHost;
	}
	//
	int getServerPort(){
		int serverPort=getPort();
		if(publicAddress!=null&&serverPort!=0){
			serverPort=publicPort;
		}
		return serverPort;
	}
	//
	void addChannel(SipChannel c){
		channels.put(c.id, c);
	}
	//
	void removeChannel(String id){
		channels.remove(id);
	}
	//--------------------------------------------------------------------------
	public SipRequest createRequest(String method,SipURI from,SipURI to){
		SipURI requestURI=SipURI.with().host(getServerHost()).port(getServerPort()).useUDP().build();
		SipRequest req=SipRequest.request(Buffers.wrap(method), requestURI.toString())
				.cseq(CSeqHeader.with().cseq(0).build())
				.from(FromHeader.with().host(from.getHost()).port(from.getPort()).user(from.getUser()).build())
				.to(ToHeader.with().host(to.getHost()).port(to.getPort()).user(from.getUser()).build())
				.build();
		return req;
	}
	//
	/**
	 * connect to remote address with UDP connection
	 * @param ip
	 * @param port
	 * @return
	 */
    public Connection connectUDP(final String ip, final int port) {
        final InetSocketAddress remoteAddress = new InetSocketAddress(ip, port);
        return new UdpConnection(this.udpListeningPoint, remoteAddress);
    }
	/**
	 * 
	 * @param msg
	 * @throws Exception 
	 */
	public void proxy(Connection connection,SipResponse msg) throws Exception {
		connection.send(msg);
	}
	//
	/**
     * Whenever we proxy a request we must also add a Via-header, which essentially says that the
     * request went "via this network address using this protocol". The {@link ViaHeader}s are used
     * for responses to find their way back the exact same path as the request took.
     * 
     * @param destination
     * @param msg
	 * @throws Exception 
     */
    public void proxyTo(Connection connection, final SipRequest msg) throws Exception {
        //final int port = destination.getPort();
        //final Connection connection = connect(
        //		destination.getHost().toString(), port == -1 ? 5060 : port);

        // SIP is pretty powerful but there are a lot of little details to get things working.
        // E.g., this sample application is acting as a stateless proxy and in order to
        // correctly relay re-transmissions or e.g. CANCELs we have to make sure to always
        // generate the same branch-id of the same request. Since a CANCEL will have the same
        // branch-id as the request it cancels, we must ensure we generate the same branch-id as
        // we did when we proxied the initial INVITE. If we don't, then the cancel will not be
        // matched by the "other" side and their phone wouldn't stop ringing.
        // SO, for this example, we'll just grab the previous value and append "-abc" to it so
        // now we are relying on the upstream element to do the right thing :-)
        //
        // See section 16.11 in RFC3263 for more information.
        final Buffer otherBranch = msg.getViaHeader().getBranch();
        final Buffer myBranch = Buffers.createBuffer(otherBranch.getReadableBytes() + 7);
        otherBranch.getBytes(myBranch);
        myBranch.write((byte) 'z');
        myBranch.write((byte) ';');
        myBranch.write((byte) 'r');
        myBranch.write((byte) 'p');
        myBranch.write((byte) 'o');
        myBranch.write((byte) 'r');
        myBranch.write((byte) 't');
        //
        ViaHeaderBuilder builder=ViaHeader.with().
        		host(getServerHost()).
        		port(getServerPort());
        if(connection.isUDP()){
        	builder.transportUDP();
        }else if(connection.isTCP()){
        	builder.transportTCP();
        }else if(connection.isWS()){
        	builder.transportWS();
        }else{
        	throw new IllegalArgumentException("not implement");
        }
        ViaHeader via = builder.branch(myBranch).build();
        // This is how you should generate the branch parameter if you are a stateful proxy:
        // Note the ViaHeader.generateBranch()...
        msg.addHeaderFirst(via);
        connection.send(msg);
    }

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return scheduledExecutorService.schedule(command, delay, unit);
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return scheduledExecutorService.scheduleAtFixedRate(command,
				initialDelay, period, unit);
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return scheduledExecutorService.scheduleWithFixedDelay(command,
				initialDelay, delay, unit);
	}
	//
	public List<SipSession>getSessions(){
		return new ArrayList<SipSession>(sessionMap.values());
	}
	public SipSession getSession(String callId){
		return sessionMap.get(callId);
	}
	/**
	 * See RFC3261 of how it is actually supposed to be done but the short
	 * version is:
	 * 
	 * For the AOR, compare the contact URI of all known bidnings and
	 * update/create/delete as needed.
	 * @param binding
	 */
	public void updateLocationBinding(final SipLocationBinding binding) {
		if(binding.getExpires()==0){
			locationStore.remove(binding);
		}else{
			locationStore.put(binding.getAor(),binding);
		}
	}
	/**
	 * get location binding by uri
	 * @param uri
	 * @return
	 */
	public SipLocationBinding getLocationBinding(SipURI uri){
		return locationStore.get(uri);
	}
	/**
	 * @return all location bindings
	 */
	public List<SipLocationBinding>getLocationBindings(){
		return new ArrayList<SipLocationBinding>(locationStore.values());
	}
	//-------------------------------------------------------------------------
	
	void messageReceived(Connection conn,SipMessage message){
		SipContext ctx=new SipContext();
		ctx.server=this;
		ctx.connection=conn;
		ctx.message=message;
		SipSession session=ctx.getSession(false);
		if(session!=null){
			session.lastAccessTime=new Date();
		}
		String traceId=(messageHandler==null)?"":messageHandler.getClass().getSimpleName();
		Jazmin.dispatcher.invokeInPool(
				traceId,
				this, 
				handlerMethod,
				Dispatcher.EMPTY_CALLBACK,ctx);
	}
	//
	public void handleMessage(SipContext ctx)throws Exception{
		SipMessage message=ctx.message;
		Connection conn=ctx.connection;
		if(messageHandler==null){
			logger.warn("can not found message handler.");
			if(message.isResponse()){
				return;
			}
			SipResponse rsp=message.createResponse(
					SipStatusCode.SC_SERVICE_UNAVAILABLE);
			conn.send(rsp);
			return;
		}
		try{
			boolean continueProcess=messageHandler.before(ctx);
			if(!continueProcess){
				return;
			}
			if(message.isRequest()){
				messageHandler.handleRequest(ctx,ctx.message.toRequest());
			}
			if(message.isResponse()){
				messageHandler.handleResponse(ctx,ctx.message.toResponse());
			}
			messageHandler.after(ctx);
		}catch(Exception e){
			logger.catching(e);
			if(message.isRequest()){
				SipResponse rsp=message.createResponse(
						SipStatusCode.SC_SERVER_INTERNAL_ERROR);
				conn.send(rsp);
			}
		}
	}
	//
	SipSession getSession(SipMessage message,boolean create){
		CallIdHeader callIDHeader=message.getCallIDHeader();
		if(callIDHeader==null){
			throw new IllegalArgumentException("bad message format,missing callId");
		}
		String callId=callIDHeader.getCallId().toString();
		if(sessionMap.containsKey(callId)){
			return sessionMap.get(callId);
		}
		if(create){
			SipSession session=new SipSession(this);
			session.setSessionTimeout(sessionTimeout);
			sessionIdLongAdder.increment();
			session.sessionId=sessionIdLongAdder.longValue();
			session.callId=callId;
			sessionMap.put(callId,session);
			return session;			
		}else{
			return null;
		}
	}
	//
	void removeSession(String callId){
		sessionMap.remove(callId);
	}
	//
	void checkSessionTimeout(){
		long now=System.currentTimeMillis();
		for(SipSession session:sessionMap.values()){
			if((now-session.lastAccessTime.getTime())>session.getSessionTimeout()*1000){
				removeSession(session.callId);
			}
		}
	}
	void checkBindingExpires(){
		long now=System.currentTimeMillis();
		for(SipLocationBinding binding:locationStore.values()){
			if((now-binding.getCreateTime().getTime())>binding.getExpires()*1000){
				locationStore.remove(binding.getAor());
			}
		}
	}
    //-------------------------------------------------------------------------
   @Override
   public void init() throws Exception {
	   ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new SipServerCommand());
		}
   }	
   //
	@Override
    public void start() throws Exception{
    	handler=new SipSocketHandler(this);
    	IOWorker ioWorker=new IOWorker("SipServerIO",
    			Runtime.getRuntime().availableProcessors()*2+1);
    	bossGroup=new NioEventLoopGroup(1,ioWorker);
    	workerGroup=new NioEventLoopGroup(0,ioWorker);
    	udpGroup=new NioEventLoopGroup(0,ioWorker);
        this.bootstrap = createUDPListeningPoint();
        this.tcpServerBootstrap = createTCPListeningPoint();
        this.webSocketServerBootstrap=createWSListeningPoint();
        startNetty();
        //session timeout checker
        Jazmin.scheduleAtFixedRate(this::checkSessionTimeout,
        		MIN_SESSION_TIMEOUT/2,
        		MIN_SESSION_TIMEOUT/2,TimeUnit.SECONDS);
        //location store timeout checker
        Jazmin.scheduleAtFixedRate(this::checkBindingExpires,
        		10,
        		10,TimeUnit.SECONDS);
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
		if(udpGroup!=null){
			udpGroup.shutdownGracefully();
		}
	}
	 //
    @Override
    public String info() {
    	InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("hostAddress",getHostAddress())
		.print("port",getPort())
		.print("webSocketPort",getWebSocketPort())
		.print("publicAddress",getPublicAddress())
		.print("publicPort",getPublicPort())
		.print("sessionTimeout",getSessionTimeout())
		.print("messageHandler",getMessageHandler());
		return ib.toString();
    }
}
