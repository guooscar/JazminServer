package jazmin.server.msg.client;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.DefaultCodecFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.util.IOUtil;

/**
 * 
 * @author icecooly
 *
 */
@Sharable
public class MessageEncoder extends MessageToByteEncoder<RequestMessage> {
	//
	private static Logger logger=LoggerFactory.get(MessageEncoder.class);
	//
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	//
	private static Charset charset=Charset.forName("UTF-8");
	//
	NetworkTrafficStat networkTrafficStat;
	public MessageEncoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	public static void encode0(
			RequestMessage msg,
			ByteBuf out,
			NetworkTrafficStat networkTrafficStat) throws Exception{
		byte body[]=null;
		if(msg.messageType==DefaultCodecFactory.FORMAT_JSON){
			body=encodeJson(msg);
		}else if(msg.messageType==DefaultCodecFactory.FORMAT_ZJSON){
			body=encodeZJson(msg);
		}else{
			throw new CorruptedFrameException("bad message type:"+msg.messageType);
		}
		int requestId=msg.requestId;
		String serviceId=msg.serviceId;
		//
		byte serviceIdBytes[]=serviceId.getBytes(charset);
		int dataLength= 4+  //package len
						2+	//payloadType
						4+	//requestId
						2+	//serviceIdLength
						serviceIdBytes.length+ //serviceId
						body.length;
		
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeShort(msg.messageType);
		out.writeInt(requestId);
		out.writeShort(serviceIdBytes.length);
		out.writeBytes(serviceIdBytes);
		out.writeBytes(body);
		networkTrafficStat.outBound(dataLength);
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			RequestMessage msg,
			ByteBuf out) throws Exception {
		encode0(msg, out, networkTrafficStat);
	}
	//
	protected static byte[] encodeJson(RequestMessage msg) throws Exception {
		String json=JSON.toJSONString(msg.requestParameters)+"\n";
    	if(logger.isDebugEnabled()){
    		logger.debug("\nencode message #{}-{} [{}]-------------\n{}",
						msg.requestId,
						msg.serviceId,
    					json);
    	}
		return json.getBytes(charset);
	}
	//
	protected static byte[] encodeZJson(RequestMessage msg) throws Exception {
		return IOUtil.compress(encodeJson(msg));
	}
}
