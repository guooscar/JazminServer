package jazmin.server.websockify;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.websockify.HostInfoProvider.HostInfo;

/**
 * Handles handshakes and messages
 */
public class WebsockifyHandler extends SimpleChannelInboundHandler<Object> {
	private static Logger logger = LoggerFactory
			.get(WebsockifyHandler.class);
	private WebSocketServerHandshaker handshaker;
	private WebsockifyServer server;
	private static final int MAX_WEBSOCKET_FRAME_SIZE=10240;
	private boolean isWss;
	public WebsockifyHandler(WebsockifyServer server,boolean isWss) {
		this.server=server;
		this.isWss=isWss;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private void handleHttpRequest(
			ChannelHandlerContext ctx,
			FullHttpRequest req) throws Exception {
		// Handle a bad request.
		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
					BAD_REQUEST));
			return;
		}
		// Allow only GET methods.
		if (req.method() != GET) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
					FORBIDDEN));
			return;
		}
		if ("/favicon.ico".equals(req.uri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, 
            		HttpResponseStatus.NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
		//
		String uri=req.uri();
		String token=null;
		if(uri.length()>1){
			token=uri.substring(1);
		}
		HostInfo hostInfo;
		if(token==null||(hostInfo=server.getHostInfoProvider().getHostInfo(token))==null){
			logger.error("can not find host info with token {}",token);
			ctx.close();
			return;
		}
		//
		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), "binary", true,MAX_WEBSOCKET_FRAME_SIZE);
		
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			String protocol = req.headers().getAsString("WebSocket-Protocol");
        	String secProtocol = req.headers().getAsString("Sec-WebSocket-Protocol");
        	if(protocol != null && secProtocol == null ){
        		req.headers().add("Sec-WebSocket-Protocol", protocol);
        	}
			handshaker.handshake(ctx.channel(), req).sync();
		}
		//
		createProxyChannel(ctx.channel(),hostInfo);
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame) 
			throws IOException {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof BinaryWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}
	
		WebsockifyChannel c=ctx.channel().attr(WebsockifyChannel.SESSION_KEY).get();
		if(c!=null){
			c.messageReceivedCount++;
			ByteBuf msg = ((BinaryWebSocketFrame) frame).content().retain();
			c.outBoundChannel.writeAndFlush(msg);
		}
	}
	//
	private static void sendHttpResponse(ChannelHandlerContext ctx,
			FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
					CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}
		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("exception on channal:" + ctx.channel(), cause);
		ctx.close();
	}
	//
	private  String getWebSocketLocation(FullHttpRequest req) {
		String location = req.headers().get("Host") + "";
		return (isWss?"wss":"ws")+"://" + location;
	}

	//
	@Override
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("channelInactive:"+ctx.channel());	
		}
		WebsockifyChannel c=ctx.channel().attr(WebsockifyChannel.SESSION_KEY).get();
		if(c!=null){
			server.removeChannel(c.id);
			c.outBoundChannel.close();
		}
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("channelActive:"+ctx.channel());	
		}
		if(server.getHostInfoProvider()==null){
			logger.error("can not find HostInfoProvider.");
			ctx.channel().close();
			return;
		}
	}
	//
	private void createProxyChannel(Channel channel,HostInfo hostInfo){
		WebsockifyChannel wsChannel=new WebsockifyChannel();
		Channel outboundChannel;
		Channel inboundChannel = channel;
		wsChannel.inBoundChannel=channel;
		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop()).channel(channel.getClass())
				.handler(new WebsockifyBackendHandler(wsChannel))
				.option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = b.connect(hostInfo.host, hostInfo.port);
		outboundChannel = f.channel();
		wsChannel.outBoundChannel=outboundChannel;
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					if (logger.isDebugEnabled()) {
						logger.debug("connect to backend {}:{} success", hostInfo.host, hostInfo.port);
					}
					// connection complete start to read first data
					inboundChannel.read();
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("connect to backend {}:{} error", hostInfo.host, hostInfo.port);
					}
					// Close the connection if the connection attempt has
					// failed.
					inboundChannel.close();
				}
			}
		});
		wsChannel.remoteAddress=hostInfo.host;
		wsChannel.remotePort=hostInfo.port;
		server.addChannel(wsChannel);
		channel.attr(WebsockifyChannel.SESSION_KEY).set(wsChannel);
		if(logger.isDebugEnabled()){
			logger.debug("session created:"+wsChannel);	
		}
	}
}