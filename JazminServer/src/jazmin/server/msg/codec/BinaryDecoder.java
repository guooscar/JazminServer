/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import jazmin.misc.NetworkTrafficStat;
/**
 * <pre>message format
 * length      			4
 * raw flag    			2
 * requestId   			4
 * serviceIdLength  	2
 * serviceId			?
 * payload				?
 * </pre>
 * @author yama
 * 26 Dec, 2014
 */
public abstract class BinaryDecoder extends ByteToMessageDecoder {
	private static final int MAX_MESSAGE_LENGTH = 1024 * 10;
	NetworkTrafficStat networkTrafficStat;
	public BinaryDecoder(NetworkTrafficStat networkTrafficStat) {
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
		//
		networkTrafficStat.inBound(dataLength+2);
		//
		int payloadType=in.readShort();
		int requestId=in.readInt();
		int serviceIdLength=in.readShort();
		byte serviceIdBytes[]=new byte[serviceIdLength];
		in.readBytes(serviceIdBytes);
		String serviceId=new String(serviceIdBytes,"UTF-8");
		//
		int payloadLength=dataLength-8-serviceIdLength;
		byte payloadBytes[]=new byte[payloadLength];
		//
		RequestMessage msg;
		if(payloadType==1){
			msg=new RequestMessage();
			msg.rawData=payloadBytes;
		}else{
			msg=decode(payloadBytes);	
		}
		//
		msg.requestId=requestId;
		msg.serviceId=serviceId;
		//
		out.add(msg);
	}
	protected abstract RequestMessage decode(byte[]payload)throws Exception;
	
}
