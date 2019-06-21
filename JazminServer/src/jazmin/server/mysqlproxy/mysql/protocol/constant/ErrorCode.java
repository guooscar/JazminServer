package jazmin.server.mysqlproxy.mysql.protocol.constant;

/**
 * 
 * <pre><b>a map of errorCode&sqlState for mysql error packet.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 * @see https://dev.mysql.com/doc/refman/5.6/en/error-messages-server.html#error_er_alter_operation_not_supported_reason_fk_check
 */
public enum ErrorCode {
	ER_HASHCHK(1000, "HY000", "hashchk"), ER_NISAMCHK(1001, "HY000", "isamchk"), ER_NO(
			1002, "HY000", "NO"), ER_YES(1003, "HY000", "YES"), ER_CANT_CREATE_FILE(
			1004, "HY000", " Can't create file '%s' (errno: %d - %s)"), ER_CANT_CREATE_DB(
			1006, "HY000", "Can't create table '%s' (errno: %d)"), ER_ACCESS_DENIED_ERROR(
			1045, "28000",
			"Access denied for user '%s'@'%s' (using password: %s)"), ER_BAD_DB_ERROR(
			1049, "42000", "Unknown database '%s'"), ER_UNKNOWN_CHARACTER_SET(
			1115, "42000", "Unknown character set: '%s'"), ER_NOT_ALLOWED_COMMAND(
			1148, "42000",
			"The used command is not allowed with this MySQL version"), ER_ERROR_DURING_COMMIT(
			1180, "HY000", "Got error %d during COMMIT");
	public int code;
	public String sqlState;
	public String message;

	private ErrorCode(int code, String sqlState, String message) {
		this.code = code;
		this.sqlState = sqlState;
		this.message = message;
	}
}
