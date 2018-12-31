package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>column count packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/com-query-response.html#text-resultset
 */
public class ColumnCountPacket extends MysqlPacket {

	public int columnCount;

	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		this.packetLength = mm.readUB3();
		this.packetId = mm.read();
		this.columnCount = (int) mm.readLength();
	}

	@Override
	public void write(ByteBuffer buffer) {
		int size = calcPacketSize();
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		BufferUtil.writeLength(buffer, columnCount);
	}

	@Override
	public int calcPacketSize() {
		int size = BufferUtil.getLength(columnCount);
		return size;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Column Count Packet";
	}

}
