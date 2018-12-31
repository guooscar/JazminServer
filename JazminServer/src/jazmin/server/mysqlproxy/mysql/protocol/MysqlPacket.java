package jazmin.server.mysqlproxy.mysql.protocol;

import java.nio.ByteBuffer;

/**
 * 
 * <pre><b>MySQLPacket is mysql's basic packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 */
public abstract class MysqlPacket {

	public static final byte COM_SLEEP = 0;

	public static final byte COM_QUIT = 1;

	public static final byte COM_INIT_DB = 2;

	public static final byte COM_QUERY = 3;

	public static final byte COM_FIELD_LIST = 4;

	public static final byte COM_CREATE_DB = 5;

	public static final byte COM_DROP_DB = 6;

	public static final byte COM_REFRESH = 7;

	public static final byte COM_SHUTDOWN = 8;

	public static final byte COM_STATISTICS = 9;

	public static final byte COM_PROCESS_INFO = 10;

	public static final byte COM_CONNECT = 11;

	public static final byte COM_PROCESS_KILL = 12;

	public static final byte COM_DEBUG = 13;

	public static final byte COM_PING = 14;

	public static final byte COM_TIME = 15;

	public static final byte COM_DELAYED_INSERT = 16;

	public static final byte COM_CHANGE_USER = 17;

	public static final byte COM_BINLOG_DUMP = 18;

	public static final byte COM_TABLE_DUMP = 19;

	public static final byte COM_CONNECT_OUT = 20;

	public static final byte COM_REGISTER_SLAVE = 21;

	public static final byte COM_STMT_PREPARE = 22;

	public static final byte COM_STMT_EXECUTE = 23;

	public static final byte COM_STMT_SEND_LONG_DATA = 24;

	public static final byte COM_STMT_CLOSE = 25;

	public static final byte COM_STMT_RESET = 26;

	public static final byte COM_SET_OPTION = 27;

	public static final byte COM_STMT_FETCH = 28;

	public int packetLength;

	public byte packetId;

	public abstract int calcPacketSize();

	protected abstract String getPacketInfo();

	public abstract void read(byte[] data);

	public abstract void write(ByteBuffer buffer);

	@Override
	public String toString() {
		return new StringBuilder().append(getPacketInfo()).append("{length=")
				.append(packetLength).append(",id=").append(packetId)
				.append('}').toString();
	}

}
