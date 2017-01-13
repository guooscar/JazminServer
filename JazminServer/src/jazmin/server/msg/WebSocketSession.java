/**
 * 
 */
package jazmin.server.msg;

import com.alibaba.fastjson.JSON;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.ResponseProto;
import jazmin.util.DumpUtil;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class WebSocketSession extends Session{
	private static Logger logger=LoggerFactory.get(WebSocketSession.class);
	MessageServer messageServer;
	WebSocketSession(NetworkChannel channel,MessageServer messageServer) {
		super(channel);
		this.messageServer=messageServer;
		connectionType="ws";
	}
	//
	void sendMessage(ResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
			TextWebSocketFrame frame=new TextWebSocketFrame(
					encodeMessage(responseMessage));
			channel.writeAndFlush(frame);
		}
	}
	//
	private String encodeMessage(ResponseMessage msg){
		//
    	ResponseProto bean=new ResponseProto();
    	bean.d=(System.currentTimeMillis());
    	bean.ri=(msg.requestId);
    	bean.rsp=(msg.responseObject);
    	bean.si=(msg.serviceId);
    	bean.sc=(msg.statusCode);
    	bean.sm=(msg.statusMessage);
    	String json=JSON.toJSONString(bean)+"\n";
    	if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n"
						+DumpUtil.formatJSON(json));
    	}
    	messageServer.networkTrafficStat.outBound(json.getBytes().length);
    	return json;
	}
}
