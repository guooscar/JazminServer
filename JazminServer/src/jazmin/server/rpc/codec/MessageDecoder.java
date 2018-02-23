package jazmin.server.rpc.codec;

import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.rpc.RpcMessage;

/**
 * @author yama
 * @date Jun 7, 2014
 */
public class MessageDecoder extends ByteToMessageDecoder {
	private static Logger logger=LoggerFactory.get(MessageDecoder.class);
	private static final int MAX_MESSAGE_LENGTH = 1024 * 1024*10;
	//
	NetworkTrafficStat networkTrafficStat;
	//
	public MessageDecoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	//
	/**
	 * 
	 */
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
			logger.error("message too long " + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
			throw new DecoderException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		networkTrafficStat.inBound(dataLength);
		byte[] decoded = new byte[dataLength];
		in.readBytes(decoded);
		//
		RpcMessage msg;
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
	    Input input = new Input(decoded);
	    msg=(RpcMessage) kryo.readClassAndObject(input);
	    input.close();
		out.add(msg);
	}
}
