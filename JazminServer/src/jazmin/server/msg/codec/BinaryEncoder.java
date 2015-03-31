package jazmin.server.msg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.misc.NetworkTrafficStat;
/**
 *<pre>message format
 * binary format
 * 10 byte header
 * 1------4 | 5--------6 7-------10 11-----------12 13-13+serviceIdlength  payload
 * length     raw payload requestId  serviceIdLength  serviceId
 
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
		int payloadType=msg.rawData==null?0:1;
		String serviceId=msg.serviceId;
		byte serviceIdBytes[]=serviceId.getBytes("UTF-8");
		int dataLength=payload.length+2+4+2+serviceIdBytes.length;
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeShort(payloadType);
		out.writeInt(requestId);
		out.writeShort(serviceIdBytes.length);
		out.writeBytes(serviceIdBytes);
		out.writeBytes(payload);
		networkTrafficStat.outBound(dataLength);
	}
	//
	protected abstract byte[] encode(ResponseMessage msg) throws Exception;
}
