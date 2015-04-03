package jazmin.server.msg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.misc.NetworkTrafficStat;
/**
 *<pre>
 * binary format header 
 * length    			4
 * raw flag  			2  
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
public abstract class BinaryEncoder extends MessageToByteEncoder<ResponseMessage> {
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	NetworkTrafficStat networkTrafficStat;
	public BinaryEncoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			ResponseMessage msg,
			ByteBuf out) throws Exception {
		byte payload[]=encode(msg);
		int requestId=msg.requestId;
		int rawFlag=msg.rawData==null?0:1;
		long timestamp=System.currentTimeMillis();
		String serviceId=msg.serviceId;
		String statusMessage=msg.statusMessage;
		int statusCode=msg.statusCode;
		//
		if(statusMessage==null){
			statusMessage="NA";
		}
		//
		byte statusMessageBytes[]=statusMessage.getBytes("UTF-8");
		byte serviceIdBytes[]=serviceId.getBytes("UTF-8");
		int dataLength= 2+  //raw flag
						4+	//requestId
						8+	//timestamp
						2+	//statusCode
						2+	//statusMsgLength
						statusMessageBytes.length+ //statusMsg
						2+	//serviceIdLength
						serviceIdBytes.length+ //serviceId
						payload.length;
		
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		//
		out.writeShort(rawFlag);
		out.writeInt(requestId);
		out.writeLong(timestamp);
		out.writeShort(statusCode);
		out.writeShort(statusMessageBytes.length);
		out.writeBytes(statusMessageBytes);
		out.writeShort(serviceIdBytes.length);
		out.writeBytes(serviceIdBytes);
		out.writeBytes(payload);
		networkTrafficStat.outBound(dataLength);
	}
	//
	protected abstract byte[] encode(ResponseMessage msg) throws Exception;
}
