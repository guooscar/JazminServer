package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>mysql drop db packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/com-drop-db.html
 */
public class DropDBPacket extends MysqlPacket {
	public byte flag;
	public byte[] schema;

	@Override
	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		flag = mm.read();
		this.schema = mm.readBytes();
	}

	@Override
	public void write(ByteBuffer buffer) {
		BufferUtil.writeUB3(buffer, calcPacketSize());
		buffer.put(packetId);
		buffer.put(COM_DROP_DB);
		buffer.put(schema);
	}

	@Override
	public int calcPacketSize() {
		int i = 1;
		i += schema.length;
		return i;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Drop DB Packet";
	}

}
