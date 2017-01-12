package jazmin.server.msg.codec;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import flex.messaging.io.amf.ASObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.MessageServer;
import jazmin.server.msg.codec.amf.AMF3Serializer;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;
/**
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
 * 26 Dec, 2014
 */
@Sharable
public class BinaryEncoder extends MessageToByteEncoder<ResponseMessage> {
	private static Logger logger=LoggerFactory.get(BinaryEncoder.class);
	//
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	private static Charset charset=Charset.forName("UTF-8");
	NetworkTrafficStat networkTrafficStat;
	public BinaryEncoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	public static void encode0(
			ResponseMessage msg,
			ByteBuf out,
			NetworkTrafficStat networkTrafficStat) throws Exception{
		byte payload[]=null;
		if(msg.messageType==MessageServer.FORMAT_JSON){
			payload=encodeJson(msg);
		}else if(msg.messageType==MessageServer.FORMAT_ZJSON){
			payload=encodeZJson(msg);
		}else if(msg.messageType==MessageServer.FORMAT_RAW){
			payload=encodeAmf3(msg);
		}else if(msg.messageType==MessageServer.FORMAT_AMF){
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
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			ResponseMessage msg,
			ByteBuf out) throws Exception {
		encode0(msg, out, networkTrafficStat);
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
		return IOUtil.compress(encodeJson(msg));
	}
}
