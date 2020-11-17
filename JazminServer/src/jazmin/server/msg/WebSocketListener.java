package jazmin.server.msg;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 
 * @author skydu
 *
 */
public interface WebSocketListener {
	
	/**
	 * 
	 * @param session
	 */
	void onCreate(WebSocketSession session);
	
	/**
	 * 
	 * @param session
	 * @param request
	 */
	void onOpen(WebSocketSession session, FullHttpRequest request);
	
	
	/**
	 * 
	 * @param session
	 * @param message
	 */
	void onMessage(WebSocketSession session, String message);
	
	/**
	 * 
	 * @param session
	 * @param message
	 */
	void onMessage(WebSocketSession session, ByteBuf message);
	
	
	/**
	 * 
	 * @param session
	 */
	void onClose(WebSocketSession session);
	
	/**
	 * 
	 * @param session
	 * @param ex
	 */
	void onError(WebSocketSession session, Throwable ex);


	
}
