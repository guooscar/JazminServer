package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>mysql auth packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see <pre>http://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse</pre>
 */
public class AuthPacket extends MysqlPacket {
	private static final byte[] FILLER = new byte[23];

	public long clientFlags;
	public long maxPacketSize;
	public int charsetIndex;
	public byte[] extra;
	public String user;
	public byte[] password;
	public String database;
	public String plugin;

	@Override
	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		clientFlags = mm.readUB4();
		maxPacketSize = mm.readUB4();
		charsetIndex = (mm.read() & 0xff);
		int current = mm.position();
		int len = (int) mm.readLength();
		if (len > 0 && len < FILLER.length) {
			byte[] ab = new byte[len];
			System.arraycopy(mm.bytes(), mm.position(), ab, 0, len);
			this.extra = ab;
		}
		mm.position(current + FILLER.length);
		user = mm.readStringWithNull();
		password = mm.readBytesWithLength();
		if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)
				&& mm.hasRemaining()) {
			database = mm.readStringWithNull();
		}
		if (((clientFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)
				&& mm.hasRemaining()) {
			plugin = mm.readStringWithNull();
		}
	}

	@Override
	public void write(ByteBuffer buffer) {
		BufferUtil.writeUB3(buffer, calcPacketSize());
		buffer.put(packetId);
		BufferUtil.writeUB4(buffer, clientFlags);
		BufferUtil.writeUB4(buffer, maxPacketSize);
		buffer.put((byte) charsetIndex);
		buffer.put(FILLER);
		if (user == null) {
			buffer.put((byte) 0);
		} else {
			BufferUtil.writeWithNull(buffer, user.getBytes());
		}
		if (password == null) {
			buffer.put((byte) 0);
		} else {
			BufferUtil.writeWithLength(buffer, password);
		}
		if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)) {
			if (database == null) {
				buffer.put((byte) 0);
			} else {
				BufferUtil.writeWithNull(buffer, database.getBytes());
			}
		}
		if (((clientFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)) {
			if (plugin == null) {
				buffer.put((byte) 0);
			} else {
				BufferUtil.writeWithNull(buffer, plugin.getBytes());
			}
		}
		
	}

	@Override
	public int calcPacketSize() {
		int size = 32;// 4+4+1+23;
		size += (user == null) ? 1 : user.length() + 1;
		size += (password == null) ? 1 : BufferUtil.getLength(password);
		if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0)) {
			size += (database == null) ? 1 : database.length() + 1;
		}
		if (((clientFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0)) {
			size += (plugin == null) ? 1 : plugin.length() + 1;
		}
		return size;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Authentication Packet";
	}

}
