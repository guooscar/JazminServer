/**
 * 
 */
package jazmin.server.msg;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class WebSocketSession extends Session{
	WebSocketMessageServer messageServer;
	WebSocketSession(Channel channel,WebSocketMessageServer messageServer) {
		super(channel);
		this.messageServer=messageServer;
	}
	//
	void sendMessage(ResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
			TextWebSocketFrame frame=new TextWebSocketFrame(
					messageServer.encodeMessage(responseMessage));
			channel.writeAndFlush(frame);
		}
	}
}
