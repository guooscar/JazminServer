package jazmin.server.mysqlproxy.mysql.proto;

import java.io.IOException;
import java.util.ArrayList;

import io.netty.buffer.ByteBuf;

public abstract class Packet {
	public long sequenceId = 0;

	public abstract ArrayList<byte[]> getPayload();
	//

	public byte[] toPacket() {
		ArrayList<byte[]> payload = this.getPayload();

		int size = 0;
		for (byte[] field : payload)
			size += field.length;

		byte[] packet = new byte[size + 4];

		System.arraycopy(Proto.buildFixedInt(3, size), 0, packet, 0, 3);
		System.arraycopy(Proto.buildFixedInt(1, this.sequenceId), 0, packet, 3, 1);

		int offset = 4;
		for (byte[] field : payload) {
			System.arraycopy(field, 0, packet, offset, field.length);
			offset += field.length;
		}

		return packet;
	}

	public static int getSize(byte[] packet) {
		int size = (int) new Proto(packet).getFixedInt(3);
		return size;
	}

	public static byte getType(byte[] packet) {
		return packet[4];
	}

	public static long getSequenceId(byte[] packet) {
		return new Proto(packet, 3).getFixedInt(1);
	}

	public static byte[] readPacket(ByteBuf in) throws IOException {
		int size = 0;
		byte[] packet = new byte[3];
		if (in.readableBytes() < 3) {
			return null;
		}
		in.markReaderIndex();
		//
		in.readBytes(packet, 0, 3);
		size = Packet.getSize(packet);
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

	//
	public static byte[] readPacketFull(ByteBuf in) throws IOException {
		if (in.readableBytes() > 0) {
			byte t[] = new byte[in.readableBytes()];
			in.readBytes(t);
			return t;
		} else {
			return null;
		}
	}
}
