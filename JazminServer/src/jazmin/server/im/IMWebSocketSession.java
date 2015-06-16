/**
 * 
 */
package jazmin.server.im;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class IMWebSocketSession extends IMSession{
	private static Logger logger=LoggerFactory.get(IMWebSocketSession.class);
	IMMessageServer messageServer;
	IMWebSocketSession(Channel channel,IMMessageServer messageServer) {
		super(channel);
		this.messageServer=messageServer;
	}
	//
	void sendMessage(IMResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
			TextWebSocketFrame frame=new TextWebSocketFrame(
					encodeMessage(responseMessage));
			channel.writeAndFlush(frame);
		}
	}
	//
	private String encodeMessage(IMResponseMessage msg){
		String json=null;
		try {
			json = new String(msg.rawData,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n"
						+DumpUtil.formatJSON(json));
    	}
    	messageServer.networkTrafficStat.outBound(json.getBytes().length);
    	return json;
	}
}
