package jazmin.server.mysqlproxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
/**
 * 
 * @author yama
 *
 */
public class PacketDecoder extends ByteToMessageDecoder {
	private int packetCount=0;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    	byte[]packet=null;
		if (packetCount >= 2) {
			packet=readPacketFull(in);
		} else {
			packet =readPacket(in);
		}
		if (packet == null) {
			return;
		}
		packetCount++;
		out.add(packet);
    }
    //
    public static byte[] readPacket(ByteBuf in) {
		int size = 0;
		byte[] packet = new byte[3];
		if (in.readableBytes() < 3) {
			return null;
		}
		in.markReaderIndex();
		//
		in.readBytes(packet, 0, 3);
		size = (int) getFixedInt(packet);
		size += 1;// sequence_id
		//
		if (in.readableBytes() < size) {
			in.resetReaderIndex();
			return null;
		}
		//
		byte[] fullPacket = new byte[size + 3];
		System.arraycopy(packet, 0, fullPacket, 0, 3);
		in.readBytes(fullPacket, 3, size);

		//
		return fullPacket;
	}
   
    public static int getFixedInt(byte[] bytes) {
    	int value = 0;
        for (int i = bytes.length-1; i > 0; i--) {
            value |= bytes[i] & 0xFF;
            value <<= 8;
        }
        value |= bytes[0] & 0xFF;
        return value;
    }
	//
	public static byte[] readPacketFull(ByteBuf in) {
		if (in.readableBytes() > 0) {
			byte t[] = new byte[in.readableBytes()];
			in.readBytes(t);
			return t;
		} else {
			return null;
		}
	}
}