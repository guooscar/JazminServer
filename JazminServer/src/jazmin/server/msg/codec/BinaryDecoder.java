/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.MessageServer;
import jazmin.server.msg.codec.amf.AMF3Deserializer;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;
/**
 * <pre>message format
 * length      			4
 * payload type    		2
 * requestId   			4
 * serviceIdLength  	2
 * serviceId			?
 * payload				?
 * </pre>
 * @author yama
 * 26 Dec, 2014
 */
public class BinaryDecoder extends ByteToMessageDecoder {
	private static Logger logger=LoggerFactory.get(BinaryDecoder.class);
	
	//
	private static final int MAX_MESSAGE_LENGTH = 1024 * 10;
	NetworkTrafficStat networkTrafficStat;
	private static Charset charset=Charset.forName("UTF-8");
	public BinaryDecoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	public static RequestMessage decode0(
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
		if (messageType == MessageServer.FORMAT_JSON) {
			decodeJson(msg,payloadBytes);
		} else if (messageType == MessageServer.FORMAT_ZJSON) {
			decodeZJson(msg,payloadBytes);
		} else if (messageType == MessageServer.FORMAT_AMF) {
			decodeAmf3(msg,payloadBytes);
		} else if (messageType == MessageServer.FORMAT_RAW) {
			msg.rawData = payloadBytes;
		}else{
			throw new CorruptedFrameException("bad message format:" + messageType);
		}
		//
		
		//
		return msg;
	}
	//
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		RequestMessage msg=decode0(in, networkTrafficStat);
		if(msg!=null){
			out.add(msg);
		}
		
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
		decodeJson(msg,IOUtil.decompress(payload));
	}
	
}
