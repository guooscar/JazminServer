package jazmin.server.msg.client;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.DefaultCodecFactory;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;
/**
 * 
 * @author icecooly
 *
 */
public class MessageDecoder extends ByteToMessageDecoder {
	//
	private static final int MAX_MESSAGE_LENGTH = 1024 * 10;
	NetworkTrafficStat networkTrafficStat;
	private static Charset charset=Charset.forName("UTF-8");
	//
	private static Logger logger=LoggerFactory.get(MessageDecoder.class);
	//
	public MessageDecoder(NetworkTrafficStat networkTrafficStat) {
	    this.networkTrafficStat=networkTrafficStat;
	}
	//
	public static ResponseMessage decode0(
			ByteBuf in,
			NetworkTrafficStat networkTrafficStat) throws Exception {
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
		short messageType = in.readShort();
		int requestId = in.readInt();
		long timestamp=in.readLong();
		int statusCode=in.readShort();
		int statusMsgLen=in.readShort();
		int serviceIdLength = in.readShort();
		byte statusMsgBytes[] = new byte[statusMsgLen];
		in.readBytes(statusMsgBytes);
		byte serviceIdBytes[] = new byte[serviceIdLength];
		in.readBytes(serviceIdBytes);
		String serviceId = new String(serviceIdBytes, charset);
		//
		int payloadLength = dataLength -20-statusMsgLen-serviceIdLength;
		byte payloadBytes[] = new byte[payloadLength];
		in.readBytes(payloadBytes);
		//
		ResponseMessage msg = new ResponseMessage();
		msg.requestId = requestId;
		msg.messageType = messageType;
		msg.serviceId = serviceId;
		msg.statusCode=statusCode;
		if (messageType == DefaultCodecFactory.FORMAT_JSON) {
			decodeJson(msg,payloadBytes);
		}else if (messageType == DefaultCodecFactory.FORMAT_ZJSON) {
			decodeZJson(msg,payloadBytes);
		}else{
			throw new CorruptedFrameException("bad message format:" + messageType);
		}
		//
		if(logger.isDebugEnabled()){
			logger.debug("ResponseMessage:{} timestamp:{}",DumpUtil.dump(msg),timestamp);
		}
		//
		return msg;
	}
	//
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		ResponseMessage msg=decode0(in, networkTrafficStat);
		if(msg!=null){
			out.add(msg);
		}
		
	}
	//
	protected static void decodeJson(ResponseMessage msg,byte[]payload)throws Exception{
		msg.responseObject=new String(payload,charset);
	}
	//
	protected static void  decodeZJson(ResponseMessage msg,byte[]payload)throws Exception{
		decodeJson(msg,IOUtil.decompress(payload));
	}
	
}
