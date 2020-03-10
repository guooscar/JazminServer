/**
 * 
 */
package jazmin.server.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class WebSocketSession extends Session{
	private static Logger logger=LoggerFactory.get(WebSocketSession.class);
	MessageServer messageServer;
	boolean isBinary;
	WebSocketSession(NetworkChannel channel,MessageServer messageServer) {
		super(channel);
		this.messageServer=messageServer;
		connectionType="ws";
	}
	//
	void sendMessage(ResponseMessage msg){
		lastAccess();
		if(channel!=null){
			sentMessageCount++;
			msg.messageType=messageType;
			if(msg.responseObject==null){
				msg.responseObject=ResponseMessage.emptyHashMap;
			}
			ByteBuf out=Unpooled.buffer(256);
			try {
				messageServer.codecFactory.encode(msg, out, messageServer.networkTrafficStat);
				if(isBinary) {
					BinaryWebSocketFrame frame=new BinaryWebSocketFrame(out);
					channel.writeAndFlush(frame);
				}else {
					TextWebSocketFrame frame=new TextWebSocketFrame(out);
					channel.writeAndFlush(frame);
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}
}
