package jazmin.server.webssh;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.webssh.ConnectionInfoProvider.ConnectionInfo;

/**
 * Handles handshakes and messages
 */
public class WebSshWebSocketHandler extends SimpleChannelInboundHandler<Object> {
	private static Logger logger = LoggerFactory
			.get(WebSshWebSocketHandler.class);
	private WebSocketServerHandshaker handshaker;
	private WebSshServer server;
	private static final int MAX_WEBSOCKET_FRAME_SIZE=10240;
	private boolean isWss;
	public WebSshWebSocketHandler(WebSshServer server,boolean isWss) {
		this.server=server;
		this.isWss=isWss;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws IOException {
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
			FullHttpRequest req) {
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
		String uri=req.uri();
		String token=null;
		if(uri.length()>1){
			token=uri.substring(1);
		}
		ConnectionInfo hostInfo;
		if(token==null||(hostInfo=server.getConnectionInfoProvider().getConnectionInfo(token))==null){
			logger.error("can not find connection info with token {}",token);
			ctx.close();
			return;
		}
		WebSshChannel webSshChannel=ctx.channel().attr(WebSshChannel.SESSION_KEY).get();
		webSshChannel.connectionInfo=hostInfo;
		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), "webssh", true,MAX_WEBSOCKET_FRAME_SIZE);
		
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
		webSshChannel.startShell();
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame) 
			throws IOException {
		WebSshChannel c=ctx.channel().attr(WebSshChannel.SESSION_KEY).get();
		c.messageReceivedCount++;
		
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}
		// Send the uppercase string back.
		String content = ((TextWebSocketFrame) frame).text();
		c.receiveMessage(content);
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
		String location = (String) req.headers().get("Host");
		return (isWss?"wss":"ws")+"://" + location;
	}

	//
	@Override
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
		WebSshChannel c=ctx.channel().attr(WebSshChannel.SESSION_KEY).get();
		if(c!=null){
			server.removeChannel(c.id);
			c.closeChannel();
			if(logger.isDebugEnabled()){
				logger.debug("channelInactive:"+ctx.channel());	
			}	
		}
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) 
			throws Exception {
		if(server.getConnectionInfoProvider()==null){
			logger.error("can not find HostInfoProvider.");
			ctx.channel().close();
			return;
		}
		WebSshChannel channel=new WebSshChannel();
		channel.channel=ctx.channel();
		channel.sshConnectTimeout=server.getDefaultSshConnectTimeout();
		channel.id=ctx.channel().id().asShortText();
		InetSocketAddress sa=(InetSocketAddress) ctx.channel().localAddress();
		//
		sa=(InetSocketAddress) ctx.channel().remoteAddress();
		channel.remoteAddress=sa.getAddress().getHostAddress();
		channel.remotePort=sa.getPort();
		server.addChannel(channel);
		ctx.channel().attr(WebSshChannel.SESSION_KEY).set(channel);
		if(logger.isDebugEnabled()){
			logger.debug("channelActive:"+ctx.channel());	
		}
	}
	//
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("userEventTriggered:"+ctx.channel()+"/"+evt);				
		}
	}
}