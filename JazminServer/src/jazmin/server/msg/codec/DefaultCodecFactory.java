/**
 * 
 */
package jazmin.server.msg.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import flex.messaging.io.amf.ASObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.CodecFactory;
import jazmin.server.msg.codec.amf.AMF3Deserializer;
import jazmin.server.msg.codec.amf.AMF3Serializer;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

/**
 *
 *<pre>
 * binary format header 
 * length    			4
 * message type  		2  
 * requestId 			4
 * timestamp			8
 * statusCode			2
 * statusMsgLength		2
 * statusMessage		? 
 * serviceIdLength      2
 * serviceId			?
 * payload				?
 * @author yama
 *
 */
public class DefaultCodecFactory implements CodecFactory{
	private static Logger logger=LoggerFactory.get(MessageEncoder.class);
	//
	public static final int FORMAT_RAW=0;
	public static final int FORMAT_JSON=1;
	public static final int FORMAT_ZJSON=2;
	public static final int FORMAT_AMF=4;
	//
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	private static Charset charset=Charset.forName("UTF-8");
	
	//
	public  void encode(
			ResponseMessage msg,
			ByteBuf out,
			NetworkTrafficStat networkTrafficStat) throws Exception{
		byte payload[]=null;
		if(msg.messageType==FORMAT_JSON){
			payload=encodeJson(msg);
		}else if(msg.messageType==FORMAT_ZJSON){
			payload=encodeZJson(msg);
		}else if(msg.messageType==FORMAT_RAW){
			payload=encodeAmf3(msg);
		}else if(msg.messageType==FORMAT_AMF){
			payload=msg.rawData;
		}else{
			throw new CorruptedFrameException("bad message type:"+msg.messageType);
		}
		int requestId=msg.requestId;
		long timestamp=System.currentTimeMillis();
		String serviceId=msg.serviceId;
		String statusMessage=msg.statusMessage;
		int statusCode=msg.statusCode;
		//
		if(statusMessage==null||statusMessage.trim().isEmpty()){
			statusMessage="NA";
		}
		//
		byte statusMessageBytes[]=statusMessage.getBytes(charset);
		byte serviceIdBytes[]=serviceId.getBytes(charset);
		int dataLength= 2+  //raw flag
						4+	//requestId
						8+	//timestamp
						2+	//statusCode
						2+	//statusMsgLength
						2+	//serviceIdLength
						statusMessageBytes.length+ //statusMsg
						serviceIdBytes.length+ //serviceId
						payload.length;
		
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		//
		out.writeShort(msg.messageType);
		out.writeInt(requestId);
		out.writeLong(timestamp);
		out.writeShort(statusCode);
		out.writeShort(statusMessageBytes.length);
		out.writeShort(serviceIdBytes.length);
		out.writeBytes(statusMessageBytes);
		out.writeBytes(serviceIdBytes);
		out.writeBytes(payload);
		networkTrafficStat.outBound(dataLength);
	}
	
	//
	@SuppressWarnings("unchecked")
	protected static byte[]  encodeAmf3(ResponseMessage msg) throws Exception {
		// amf3 format
		ASObject obj = new ASObject();
		obj.put("msg", msg.responseObject);
		//
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		AMF3Serializer ser = new AMF3Serializer(stream);
		ser.writeObject(obj);
		ser.flush();
		ser.close();
		byte encodedByte[] = stream.toByteArray();
		stream.close();
		return encodedByte;
	}
	//
	protected static byte[] encodeJson(ResponseMessage msg) throws Exception {
		String json=JSON.toJSONString(msg.responseObject)+"\n";
    	if(logger.isDebugEnabled()){
    		logger.debug("\nencode message #{}-{} [{}-{}]-------------\n{}",
						msg.requestId,
						msg.serviceId,
						msg.statusCode,
						msg.statusMessage,
    					DumpUtil.formatJSON(json));
    	}
		return json.getBytes(charset);
	}
	//
	protected static byte[] encodeZJson(ResponseMessage msg) throws Exception {
		return IOUtil.gzipCompress(encodeJson(msg));
	}
	//
	
	//--------------------------------------------------------------------------------
	//
	public  RequestMessage decode(
			ByteBuf in,
			NetworkTrafficStat networkTrafficStat) throws Exception {
		// Wait until the length prefix is available.
		if (in.readableBytes() < 4) {
			return null;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (dataLength > MAX_MESSAGE_LENGTH) {
			in.resetReaderIndex();
			throw new CorruptedFrameException("message too long" + dataLength + "/" + MAX_MESSAGE_LENGTH);
		}
		if (in.readableBytes() < dataLength - 4) {
			in.resetReaderIndex();
			return null;
		}
		//
		networkTrafficStat.inBound(dataLength + 4);
		//
		int messageType = in.readShort();
		int requestId = in.readInt();
		int serviceIdLength = in.readShort();
		byte serviceIdBytes[] = new byte[serviceIdLength];
		in.readBytes(serviceIdBytes);
		String serviceId = new String(serviceIdBytes, charset);
		//
		int payloadLength = dataLength - serviceIdLength - 12;
		byte payloadBytes[] = new byte[payloadLength];
		in.readBytes(payloadBytes);
		//
		RequestMessage msg = new RequestMessage();
		msg.messageType = messageType;
		msg.requestId = requestId;
		msg.serviceId = serviceId;
		if (messageType == FORMAT_JSON) {
			decodeJson(msg,payloadBytes);
		} else if (messageType == FORMAT_ZJSON) {
			decodeZJson(msg,payloadBytes);
		} else if (messageType == FORMAT_AMF) {
			decodeAmf3(msg,payloadBytes);
		} else if (messageType == FORMAT_RAW) {
			msg.rawData = payloadBytes;
		}else{
			throw new CorruptedFrameException("bad message format:" + messageType);
		}
		//
		
		//
		return msg;
	}
	
	protected static void  decodeAmf3(RequestMessage msg,byte[]payload)throws Exception{
		ByteArrayInputStream bais=new ByteArrayInputStream(payload);
		AMF3Deserializer des=new AMF3Deserializer(bais);
		Object message = des.readObject();
		@SuppressWarnings("unchecked")
		Map<String,Object> obj=(Map<String,Object>) message;
		@SuppressWarnings("unchecked")
		List<String>rps=(List<String>) obj.get("rps");
		
		bais.close();
		des.close();
		msg.requestParameters= rps.toArray(new String[rps.size()]);
	}
	//
	protected static void decodeJson(RequestMessage msg,byte[]payload)throws Exception{
		String s = new String(payload, charset);
		JSONArray array=JSON.parseArray(s);
		if (logger.isDebugEnabled()) {
			logger.debug(
					"\ndecode message #{}-{} \n{} ",
					msg.requestId,
					msg.serviceId,
					DumpUtil.formatJSON(s));
		}
		String ret[]=new String[array.size()];
		for(int i=0;i<array.size();i++){
			ret[i]=array.getString(i);
		}
		msg.requestParameters=ret;
	}
	//
	protected static void  decodeZJson(RequestMessage msg,byte[]payload)throws Exception{
		decodeJson(msg,IOUtil.gzipDecompress(payload));
	}
	
}
