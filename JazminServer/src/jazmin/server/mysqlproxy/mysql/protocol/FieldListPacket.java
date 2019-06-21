package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>mysql field list packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/com-field-list.html
 */
public class FieldListPacket extends MysqlPacket {
	public byte flag;
	public byte[] table;
	public byte[] fieldWildcard;

	@Override
	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		flag = mm.read();
		table = mm.readBytesWithNull();
		fieldWildcard = mm.readBytes();
	}

	@Override
	public void write(ByteBuffer buffer) {
		BufferUtil.writeUB3(buffer, calcPacketSize());
		buffer.put(packetId);
		buffer.put(COM_FIELD_LIST);
		BufferUtil.writeWithNull(buffer, table);
		buffer.put(fieldWildcard);
	}

	@Override
	public int calcPacketSize() {
		int i = 1;
		i += table.length + 1;
		i += fieldWildcard.length;
		return i;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Field List Packet";
	}

}
