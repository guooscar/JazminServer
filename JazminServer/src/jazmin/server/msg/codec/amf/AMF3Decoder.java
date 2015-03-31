/**
 * 
 */
package jazmin.server.msg.codec.amf;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryDecoder;
import jazmin.server.msg.codec.RequestMessage;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class AMF3Decoder extends BinaryDecoder{
	
	public AMF3Decoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
	}

	@Override
	protected RequestMessage decode(int requestId, String serviceId,
			byte[] payload) throws Exception {
		ByteArrayInputStream bais=new ByteArrayInputStream(payload);
		AMF3Deserializer des=new AMF3Deserializer(bais);
		Object message = des.readObject();
		//
		RequestMessage reqMessage=new RequestMessage();
		@SuppressWarnings("unchecked")
		Map<String,Object> obj=(Map<String,Object>) message;
		String ri=(String) obj.get("ri");
		String si=(String) obj.get("si");
		@SuppressWarnings("unchecked")
		List<String>rps=(List<String>) obj.get("rps");
		int idx=0;
		for(String ss:rps){
			if(idx<RequestMessage.MAX_PARAMETER_COUNT){
				reqMessage.requestParameters[idx]=ss;
			}
			idx++;
		}
		reqMessage.serviceId=si;
		reqMessage.requestId=Integer.valueOf(ri);
		//
		bais.close();
		des.close();
		return reqMessage;
	}
}
