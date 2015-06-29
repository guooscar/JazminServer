package jazmin.server.rpc.codec.zjson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RpcMessage;
import jazmin.util.IOUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class CompressedJSONEncoder extends MessageToByteEncoder<RpcMessage> {
	private static final int MAX_MESSAGE_LENGTH=1024*1024*10;
	private static Logger logger=LoggerFactory.get(CompressedJSONEncoder.class);
	//
	NetworkTrafficStat networkTrafficStat;
	public CompressedJSONEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			RpcMessage msg,
			ByteBuf out) throws Exception {
		byte payloadBytes[]=JSON.toJSONBytes(msg,SerializerFeature.WriteClassName);
		payloadBytes=IOUtil.compress(payloadBytes);
		int dataLength=payloadBytes.length;
		if(dataLength>MAX_MESSAGE_LENGTH){
			logger.fatal("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
			throw new CorruptedFrameException("message too long." + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		out.writeInt(dataLength);
		out.writeBytes(payloadBytes);
		networkTrafficStat.outBound(dataLength);
	}
}
