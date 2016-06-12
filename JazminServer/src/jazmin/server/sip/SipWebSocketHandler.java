package jazmin.server.sip;

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
import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.impl.SipParser;
import jazmin.server.sip.stack.Clock;
import jazmin.server.sip.stack.Connection;
import jazmin.server.sip.stack.DefaultSipMessageEvent;
import jazmin.server.sip.stack.SipMessageEvent;
import jazmin.server.sip.stack.SystemClock;
import jazmin.server.sip.stack.WsConnection;

/**
 * Handles handshakes and messages
 */
public class SipWebSocketHandler extends SimpleChannelInboundHandler<Object> {
	private static Logger logger = LoggerFactory
			.get(SipWebSocketHandler.class);
	private static final String WEBSOCKET_PATH = "/ws";
	private WebSocketServerHandshaker handshaker;
	private final Clock clock;
	private SipServer server;
	private static final int MAX_WEBSOCKET_FRAME_SIZE=10240;
	public SipWebSocketHandler(SipServer server) {
		clock=new SystemClock();
		this.server=server;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws IOException {
		SipChannel c=ctx.channel().attr(SipChannel.SESSION_KEY).get();
		c.messageReceivedCount++;
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
		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), "sip", true,MAX_WEBSOCKET_FRAME_SIZE);
		
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
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
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}
		// Send the uppercase string back.
		String content = ((TextWebSocketFrame) frame).text();
		if (content.length() < 20) {
            return;
        }
		final Buffer buffer = Buffers.wrap(content);
        SipParser.consumeSWS(buffer);
        final SipMessage sipMessage = SipParser.frame(buffer);
        if (logger.isDebugEnabled()) {
			logger.debug("<<<<<<<<<<receive from {}\n{}", ctx.channel(),
					sipMessage);
		}
        final Connection connection = new WsConnection(
        		ctx.channel(),
        		(InetSocketAddress) ctx.channel().remoteAddress());
        final SipMessageEvent event = new DefaultSipMessageEvent(
        		connection, 
        		sipMessage,
        		clock.getCurrentTimeMillis());
        server.messageReceived(event.getConnection(),event.getMessage());
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
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
		server.removeChannel(ctx.channel().id().asShortText());
		if(logger.isDebugEnabled()){
			logger.debug("channelInactive:"+ctx.channel());	
		}
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) 
			throws Exception {
		SipChannel channel=new SipChannel();
		channel.id=ctx.channel().id().asShortText();
		channel.transport="ws";
		InetSocketAddress sa=(InetSocketAddress) ctx.channel().localAddress();
		channel.localAddress=sa.getAddress().getHostAddress();
		channel.localPort=sa.getPort();
		//
		sa=(InetSocketAddress) ctx.channel().remoteAddress();
		channel.remoteAddress=sa.getAddress().getHostAddress();
		channel.remotePort=sa.getPort();
		server.addChannel(channel);
		ctx.channel().attr(SipChannel.SESSION_KEY).set(channel);
		if(logger.isDebugEnabled()){
			logger.debug("channelActive:"+ctx.channel());	
		}
	}
	//
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		logger.debug("userEventTriggered:"+ctx.channel());	
	}
}