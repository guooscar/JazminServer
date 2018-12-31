package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>mysql query packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/com-query.html
 */
public class QueryPacket extends MysqlPacket {
	public byte flag;
	public byte[] message;

	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		flag = mm.read();
		message = mm.readBytes();
	}

	public void write(ByteBuffer buffer) {
		int size = calcPacketSize();
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		buffer.put(COM_QUERY);
		buffer.put(message);
	}

	@Override
	public int calcPacketSize() {
		int size = 1;
		if (message != null) {
			size += message.length;
		}
		return size;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Query Packet";
	}

}
