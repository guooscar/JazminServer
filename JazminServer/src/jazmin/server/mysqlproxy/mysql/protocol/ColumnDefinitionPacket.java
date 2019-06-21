package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

import jazmin.server.mysqlproxy.mysql.protocol.util.BufferUtil;

/**
 * 
 * <pre><b>column definition command packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see http://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition
 */
public class ColumnDefinitionPacket extends MysqlPacket {
	private static final byte[] DEFAULT_CATALOG = "def".getBytes();
	private static final byte NEXT_LENGTH = 0x0c;
	private static final byte[] FILLER = { 00, 00 };

	public byte[] catalog = DEFAULT_CATALOG;// always "def"
	public byte[] schema;
	public byte[] table;
	public byte[] orgTable;
	public byte[] name;
	public byte[] orgName;
	public byte nextLength = NEXT_LENGTH;// always 0x0c
	public int charsetSet;
	public long length;
	public int type;
	public int flags;
	public byte decimals;
	public byte[] filler = FILLER;
	public byte[] defaultValues;

	public void read(byte[] data) {
		MysqlMessage mm = new MysqlMessage(data);
		this.packetLength = mm.readUB3();
		this.packetId = mm.read();
		this.catalog = mm.readBytesWithLength();
		this.schema = mm.readBytesWithLength();
		this.table = mm.readBytesWithLength();
		this.orgTable = mm.readBytesWithLength();
		this.name = mm.readBytesWithLength();
		this.orgName = mm.readBytesWithLength();
		this.nextLength = mm.read();
		this.charsetSet = mm.readUB2();
		this.length = mm.readUB4();
		this.type = mm.read() & 0xff;
		this.flags = mm.readUB2();
		this.decimals = mm.read();
		this.filler = mm.readBytes(2);
		if (mm.hasRemaining()) {
			this.defaultValues = mm.readBytesWithLength();
		}
	}

	@Override
	public void write(ByteBuffer buffer) {
		int size = calcPacketSize();
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		BufferUtil.writeWithLength(buffer, catalog, (byte) 0);
		BufferUtil.writeWithLength(buffer, schema, (byte) 0);
		BufferUtil.writeWithLength(buffer, table, (byte) 0);
		BufferUtil.writeWithLength(buffer, orgTable, (byte) 0);
		BufferUtil.writeWithLength(buffer, name, (byte) 0);
		BufferUtil.writeWithLength(buffer, orgName, (byte) 0);
		buffer.put(NEXT_LENGTH);
		BufferUtil.writeUB2(buffer, charsetSet);
		BufferUtil.writeUB4(buffer, length);
		buffer.put((byte) (type & 0xff));
		BufferUtil.writeUB2(buffer, flags);
		buffer.put(decimals);
		buffer.put(FILLER);
		if (defaultValues != null) {
			//only use for show columns
			BufferUtil.writeWithLength(buffer, defaultValues);
		}
	}

	@Override
	public int calcPacketSize() {
		int size = (catalog == null ? 1 : BufferUtil.getLength(catalog));
		size += (schema == null ? 1 : BufferUtil.getLength(schema));
		size += (table == null ? 1 : BufferUtil.getLength(table));
		size += (orgTable == null ? 1 : BufferUtil.getLength(orgTable));
		size += (name == null ? 1 : BufferUtil.getLength(name));
		size += (orgName == null ? 1 : BufferUtil.getLength(orgName));
		size += 13;
		if (defaultValues != null) {
			size += BufferUtil.getLength(defaultValues);
		}
		return size;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Column Definition Packet";
	}

}
