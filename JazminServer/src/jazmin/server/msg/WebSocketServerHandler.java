package jazmin.server.msg;

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
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.RequestMessage;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private static Logger logger = LoggerFactory
			.get(WebSocketServerHandler.class);
	//
	private static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("s");
	private static final String WEBSOCKET_PATH = "/websocket";
	private WebSocketServerHandshaker handshaker;
	private MessageServer messageServer;

	public WebSocketServerHandler(MessageServer messageServer) {
		this.messageServer = messageServer;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) {
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

	private void handleHttpRequest(ChannelHandlerContext ctx,
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
		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx
					.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame) {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (frame instanceof PongWebSocketFrame) {
			return;
		}
		if (frame instanceof BinaryWebSocketFrame || frame instanceof TextWebSocketFrame) {
			ByteBuf content = frame.content();
			RequestMessage message;
			try {
				message = decodeMessage(content);
				if(message!=null){
					Session session = ctx.channel().attr(SESSION_KEY).get();
					if(frame instanceof BinaryWebSocketFrame) {
						((WebSocketSession)session).isBinary=true;
					}
					messageServer.receiveMessage(session, message);
				}
			} catch (Exception e) {
				logger.catching(e);
			}
			return;
		}
		throw new UnsupportedOperationException(String.format(
				"%s frame types not supported", frame.getClass().getName()));
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
	private static String getWebSocketLocation(FullHttpRequest req) {
		String location = req.headers().get("Host") + WEBSOCKET_PATH;
		return "ws://" + location;
	}

	//
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Session session = ctx.channel().attr(SESSION_KEY).get();
		messageServer.sessionDisconnected(session);
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		WebSocketSession session = new WebSocketSession(
				new NettyNetworkChannel(ctx.channel()),
				messageServer);
		ctx.channel().attr(SESSION_KEY).set(session);
		messageServer.sessionCreated(session);
	}

	//
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			Session session = ctx.channel().attr(SESSION_KEY).get();
			messageServer.sessionIdle(session);
		}
	}
	//
	//--------------------------------------------------------------------------
	private RequestMessage decodeMessage(ByteBuf receiveBuffer)throws Exception{
		RequestMessage req = messageServer.codecFactory.decode(receiveBuffer, messageServer.networkTrafficStat);
		return req;
	}
	
}