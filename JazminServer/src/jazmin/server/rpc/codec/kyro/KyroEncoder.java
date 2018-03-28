package jazmin.server.rpc.codec.kyro;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RpcMessage;
import jazmin.server.rpc.codec.CodecUtil;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class KyroEncoder extends MessageToByteEncoder<RpcMessage> {
	private static Logger logger=LoggerFactory.get(KyroEncoder.class);
	private static final int MAX_MESSAGE_LENGTH=1024*1024*10;
	//
	NetworkTrafficStat networkTrafficStat;
	//
	public KyroEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	protected void encode(
			ChannelHandlerContext ctx, 
			RpcMessage msg,
			ByteBuf out) throws Exception {
		try{
			writeMessage(ctx, msg, out);
		}catch(Exception e){
			writeMessage(ctx, 
					CodecUtil.createExceptionMessage(msg.id, e.getMessage()), out);
		}
	}
	//
	private void writeMessage(
			ChannelHandlerContext ctx, 
			RpcMessage msg,
			ByteBuf out) throws Exception {
		
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
        Output output = new Output(bos);
        kryo.writeClassAndObject(output, msg);
        output.flush();
		byte payloadBytes[]= bos.toByteArray();
		int dataLength=payloadBytes.length;
		if(dataLength>MAX_MESSAGE_LENGTH){
			logger.error("message too long " + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
			throw new EncoderException("message too long." + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeBytes(payloadBytes);
		networkTrafficStat.outBound(dataLength);
	}
}
