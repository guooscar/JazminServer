package jazmin.server.rpc.codec.json;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RPCMessage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class JSONEncoder extends MessageToByteEncoder<RPCMessage> {
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	//
	NetworkTrafficStat networkTrafficStat;
	public JSONEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			RPCMessage msg,
			ByteBuf out) throws Exception {
		byte payloadBytes[]=JSON.toJSONBytes(msg,SerializerFeature.WriteClassName);
		int dataLength=payloadBytes.length;
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long." + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeBytes(payloadBytes);
		networkTrafficStat.outBound(dataLength);
	}
}
