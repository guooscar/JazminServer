package jazmin.server.msg.codec.amf;

import flex.messaging.io.amf.ASObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.ResponseProto;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
@Sharable
public class AMF3Encoder extends MessageToByteEncoder<ResponseMessage> {
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	NetworkTrafficStat networkTrafficStat;
	public AMF3Encoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	@SuppressWarnings("unchecked")
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			ResponseMessage msg,
			ByteBuf out) throws Exception {
		if(msg.rawData!=null){
			out.writeBytes(msg.rawData);
			return;
		}
		//
		ResponseProto bean=new ResponseProto();
    	bean.d=(System.currentTimeMillis());
    	bean.ri=(msg.requestId);
    	bean.rsp=(msg.responseMessages);
    	bean.si=(msg.serviceId);
    	bean.sc=(msg.statusCode);
    	bean.sm=(msg.statusMessage);
		
		//amf3 format
		ASObject obj=new ASObject();
		obj.put("msg", bean);
		//
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		AMF3Serializer ser = new AMF3Serializer(stream);
		ser.writeObject(obj);
		ser.flush();
		ser.close();
		byte encodedByte[] = stream.toByteArray();
		stream.close();
		int dataLength=encodedByte.length;
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeBytes(encodedByte);
		networkTrafficStat.outBound(dataLength);
	}
}
