/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec.amf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.RequestMessage;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class AMF3Decoder extends ByteToMessageDecoder {
	private static final int MAX_MESSAGE_LENGTH = 1024 * 10;
	NetworkTrafficStat networkTrafficStat;
	public AMF3Decoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// Wait until the length prefix is available.
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (dataLength > MAX_MESSAGE_LENGTH) {
			in.resetReaderIndex();
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		networkTrafficStat.inBound(dataLength);
		byte[] decoded = new byte[dataLength];
		in.readBytes(decoded);
		ByteArrayInputStream bais=new ByteArrayInputStream(decoded);
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
		out.add(reqMessage);
		bais.close();
		des.close();
	}

}
