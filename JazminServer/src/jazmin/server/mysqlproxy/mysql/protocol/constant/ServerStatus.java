package jazmin.server.mysqlproxy.mysql.protocol.constant;

/**
 * 
 * <pre><b>server status.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see https://dev.mysql.com/doc/internals/en/status-flags.html
 */
public class ServerStatus {
	// a transaction is active
	public static int SERVER_STATUS_IN_TRANS = 0x0001;
	// auto-commit is enabled
	public static int SERVER_STATUS_AUTOCOMMIT = 0x0002;
	public static int SERVER_MORE_RESULTS_EXISTS = 0x0008;
	public static int SERVER_STATUS_NO_GOOD_INDEX_USED = 0x0010;
	public static int SERVER_STATUS_NO_INDEX_USED = 0x0020;
	// Used by Binary Protocol Resultset to signal that COM_STMT_FETCH must be
	// used to fetch the row-data.
	public static int SERVER_STATUS_CURSOR_EXISTS = 0x0040;
	public static int SERVER_STATUS_LAST_ROW_SENT = 0x0080;
	public static int SERVER_STATUS_DB_DROPPED = 0x0100;
	public static int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 0x0200;
	public static int SERVER_STATUS_METADATA_CHANGED = 0x0400;
	public static int SERVER_QUERY_WAS_SLOW = 0x0800;
	public static int SERVER_PS_OUT_PARAMS = 0x1000;
	// in a read-only transaction
	public static int SERVER_STATUS_IN_TRANS_READONLY = 0x2000;
	// connection state information has changed
	public static int SERVER_SESSION_STATE_CHANGED = 0x4000;
}
