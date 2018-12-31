package jazmin.server.mysqlproxy;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jazmin.server.mysqlproxy.mysql.proto.Packet;
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
			packet=Packet.readPacketFull(in);
		} else {
			packet = Packet.readPacket(in);
		}
		if (packet == null) {
			return;
		}
		packetCount++;
		out.add(packet);
    }
}