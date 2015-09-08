package jazmin.server.msg.codec;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class JSONRequestParser {
	//
	private static Logger logger=LoggerFactory.get(JSONRequestParser.class);
	//
	public static RequestMessage createRequestMessage(String json){
		RequestMessage msg=new RequestMessage();
		JSONObject jsonObj=null;
		try{
			jsonObj=JSON.parseObject(json.trim());
		}catch(Exception e){
			logger.warn("bad message {}",e.getMessage());
			msg.isBadRequest=true;
			return msg;
		}
		if(jsonObj==null){
			logger.warn("bad message.json string empty.");
			msg.isBadRequest=true;
			return msg;
		}
		String si=jsonObj.getString("si");
		Integer ri=jsonObj.getInteger("ri");
		JSONArray rps=jsonObj.getJSONArray("rps");
		if(si==null||ri==null||rps==null){
			logger.warn("bad message."+json);
			msg.isBadRequest=true;
			return msg;
		}
		msg.serviceId=si;
		msg.requestId=ri;
		for(int i=0;i<rps.size();i++){
			if(i<RequestMessage.MAX_PARAMETER_COUNT){
				msg.requestParameters[i]=rps.getString(i);	
			}else{
				logger.warn("drop request parameter,size >"+
						RequestMessage.MAX_PARAMETER_COUNT+"/"+rps.getString(i));
			}
		}
		msg.isBadRequest=false;
		//
		return msg;
	}
	//
	public static RequestMessage createSimpleRequestMessage(String json){
		RequestMessage msg=new RequestMessage();
		JSONObject jsonObj=null;
		try{
			jsonObj=JSON.parseObject(json.trim());
		}catch(Exception e){
			logger.warn("bad message {}",e.getMessage());
			msg.isBadRequest=true;
			return msg;
		}
		if(jsonObj==null){
			logger.warn("bad message.json string empty.");
			msg.isBadRequest=true;
			return msg;
		}
		JSONArray rps=jsonObj.getJSONArray("rps");
		for(int i=0;i<rps.size();i++){
			if(i<RequestMessage.MAX_PARAMETER_COUNT){
				msg.requestParameters[i]=rps.getString(i);	
			}else{
				logger.warn("drop request parameter,size >"+
						RequestMessage.MAX_PARAMETER_COUNT+"/"+rps.getString(i));
			}
		}
		msg.isBadRequest=false;
		//
		return msg;
	}
}
