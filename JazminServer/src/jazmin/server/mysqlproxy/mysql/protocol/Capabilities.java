package jazmin.server.mysqlproxy.mysql.protocol;

/**
 * 
 * <pre>
 * <b>server capabilities.</b>
 * </pre>
 * 
 * @author <pre>
 * seaboat
 * </pre>
 * 
 *         <pre>
 * <b>email: </b>849586227@qq.com
 * </pre>
 * 
 *         <pre>
 * <b>blog: </b>http://blog.csdn.net/wangyangzhizhou
 * </pre>
 * @version 1.0
 * @see http
 *      ://dev.mysql.com/doc/internals/en/capability-flags.html#packet-Protocol
 *      ::CapabilityFlags
 */
public interface Capabilities {

	int CLIENT_LONG_PASSWORD = 1;
	int CLIENT_FOUND_ROWS = 2;
	int CLIENT_LONG_FLAG = 4;
	int CLIENT_CONNECT_WITH_DB = 8;
	int CLIENT_NO_SCHEMA = 16;
	int CLIENT_COMPRESS = 32;
	int CLIENT_ODBC = 64;
	int CLIENT_LOCAL_FILES = 128;
	int CLIENT_IGNORE_SPACE = 256;
	int CLIENT_PROTOCOL_41 = 512;
	int CLIENT_INTERACTIVE = 1024;
	int CLIENT_SSL = 2048;
	int CLIENT_IGNORE_SIGPIPE = 4096;
	int CLIENT_TRANSACTIONS = 8192;
	int CLIENT_RESERVED = 16384;
	int CLIENT_SECURE_CONNECTION = 32768;
	int CLIENT_MULTI_STATEMENTS = 65536;
	int CLIENT_MULTI_RESULTS = 131072;
	//
	int CLIENT_PLUGIN_AUTH = 1 << 19;
	int CLIENT_CONNECT_ATTRS = 1 << 20;
	int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 1 << 21;

}
