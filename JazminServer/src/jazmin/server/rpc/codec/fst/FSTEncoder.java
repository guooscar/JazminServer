package jazmin.server.rpc.codec.fst;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RpcMessage;

import org.nustaq.serialization.FSTConfiguration;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
@Sharable
public class FSTEncoder extends MessageToByteEncoder<RpcMessage> {
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	//
	NetworkTrafficStat networkTrafficStat;
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	//
	public FSTEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			RpcMessage msg,
			ByteBuf out) throws Exception {
		byte payloadBytes[]=conf.asByteArray(msg);
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
