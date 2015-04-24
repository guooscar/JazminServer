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

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.aop.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.stack.Connection;
import jazmin.server.sip.stack.SipMessageDatagramDecoder;
import jazmin.server.sip.stack.SipMessageEncoder;
import jazmin.server.sip.stack.SipMessageStreamDecoder;
import jazmin.server.sip.stack.UdpConnection;

/**
 * @author yama
 *
 */
public class SipServer extends Server{
	private static Logger logger=LoggerFactory.get(SipServer.class);
	//
	private String ip;
    private int port;
    private int sessionTimeout;
    private static final int MIN_SESSION_TIMEOUT=60;
    private  EventLoopGroup bossGroup;
    private  EventLoopGroup workerGroup;
    private  EventLoopGroup udpGroup;
    private  SipHandler handler;
    private  ServerBootstrap serverBootstrap;
    private  Bootstrap bootstrap;
    private  Channel udpListeningPoint = null;
    private  SipMessageHandler messageHandler;
    private  Method handlerMethod;
    private  Map<String,SipSession>sessionMap;
    private  LongAdder sessionIdLongAdder;
    //
    public SipServer() {
        this.ip = "127.0.0.1";
        this.port = 5060;
        handlerMethod=Dispatcher.getMethod(
        		SipServer.class,"handleMessage",
        		SipContext.class);
        sessionMap=new ConcurrentHashMap<String, SipSession>();
        sessionIdLongAdder=new LongAdder();
        sessionTimeout=60;
    }
    //
    public Connection connect(final String ip, final int port) {
        final InetSocketAddress remoteAddress = new InetSocketAddress(ip, port);
        return new UdpConnection(this.udpListeningPoint, remoteAddress);
    }
    //
    private void startNetty() throws Exception {
    	final InetSocketAddress socketAddress = new InetSocketAddress(this.ip, this.port);
        this.udpListeningPoint = this.bootstrap.bind(socketAddress).sync().channel();
        this.serverBootstrap.bind(socketAddress).sync();
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
    //-------------------------------------------------------------------------
    /**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		if(isInited()){
			throw new IllegalStateException("set before inited");
		}
		this.ip = ip;
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
	public void handleMessage(SipContext ctx){
		SipMessage message=ctx.message;
		Connection conn=ctx.connection;
		if(messageHandler==null){
			logger.warn("can not found message handler.");
			if(message.isResponse()){
				return;
			}
			SipResponse rsp=message.toRequest().createResponse(
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
			SipResponse rsp=message.toRequest().createResponse(
					SipStatusCode.SC_SERVER_INTERNAL_ERROR);
			conn.send(rsp);
		}
	}
	//
	SipSession getSession(SipMessage message,boolean create){
		String callId=message.getCallIDHeader().getCallId().toString();
		if(sessionMap.containsKey(callId)){
			return sessionMap.get(callId);
		}
		if(create){
			SipSession session=new SipSession(this);
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
			if((now-session.lastAccessTime.getTime())>sessionTimeout*1000){
				removeSession(session.callId);
			}
		}
	}
    //-------------------------------------------------------------------------
    //
	@Override
    public void start() throws Exception{
    	handler=new SipHandler(this);
    	IOWorker ioWorker=new IOWorker("SipServerIO",
    			Runtime.getRuntime().availableProcessors()*2+1);
    	bossGroup=new NioEventLoopGroup(1,ioWorker);
    	workerGroup=new NioEventLoopGroup(0,ioWorker);
    	udpGroup=new NioEventLoopGroup(0,ioWorker);
        this.bootstrap = createUDPListeningPoint();
        this.serverBootstrap = createTCPListeningPoint();
        startNetty();
        //session timeout checker
        Jazmin.scheduleAtFixedRate(this::checkSessionTimeout,
        		MIN_SESSION_TIMEOUT/2,
        		MIN_SESSION_TIMEOUT/2,TimeUnit.SECONDS);
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
		.print("port",getPort())
		.print("ip",getIp())
		.print("sessionTimeout",getSessionTimeout())
		.print("messageHandler",getMessageHandler());
		return ib.toString();
    }
}
