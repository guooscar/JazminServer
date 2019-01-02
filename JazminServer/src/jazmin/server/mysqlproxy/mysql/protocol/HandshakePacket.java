package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>AuthPacket means mysql initial handshake packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake
 */
public class HandshakePacket extends MysqlPacket {
	private static final byte[] FILLER_13 = new byte[] { 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0 };

	public byte protocolVersion;
	public String serverVersion;
	public long threadId;
	public byte[] seed;
	public int serverCapabilities;
	public byte serverCharsetIndex;
	public int serverStatus;
	public byte[] restOfScrambleBuff;

	@Override
	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		protocolVersion = mm.read();
		serverVersion = mm.readStringWithNull();
		threadId = mm.readUB4();
		seed = mm.readBytesWithNull();
		serverCapabilities = mm.readUB2();
		serverCharsetIndex = mm.read();
		serverStatus = mm.readUB2();
		mm.move(13);
		restOfScrambleBuff = mm.readBytesWithNull();
	}

	@Override
	public int calcPacketSize() {
		int size = 1;
		size += serverVersion.getBytes().length;
		size += 5;
		size += seed.length;
		size += 19;
		size += restOfScrambleBuff.length;
		size += 1;
		return size;
	}

	@Override
	public void write(ByteBuffer buffer) {
		BufferUtil.writeUB3(buffer, calcPacketSize());
		buffer.put(packetId);
		buffer.put(protocolVersion);
		BufferUtil.writeWithNull(buffer, serverVersion.getBytes());
		BufferUtil.writeUB4(buffer, threadId);
		BufferUtil.writeWithNull(buffer, seed);
		BufferUtil.writeUB2(buffer, serverCapabilities);
		buffer.put(serverCharsetIndex);
		BufferUtil.writeUB2(buffer, serverStatus);
		buffer.put(FILLER_13);
		BufferUtil.writeWithNull(buffer, restOfScrambleBuff);
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Handshake Packet";
	}

}