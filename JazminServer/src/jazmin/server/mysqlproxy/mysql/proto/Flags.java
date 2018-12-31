package jazmin.server.mysqlproxy.mysql.proto;

/*
 * Just a list of important flags that the Proxy code uses.
 *
 */

public class Flags {
    public static final int HOSTNAME_LENGTH                         = 60;
    public static final int SYSTEM_CHARSET_MBMAXLEN                 = 3;
    public static final int NAME_CHAR_LEN                           = 64;
    public static final int USERNAME_CHAR_LENGTH                    = 16;
    public static final int NAME_LEN                                = NAME_CHAR_LEN * SYSTEM_CHARSET_MBMAXLEN;
    public static final int USERNAME_LENGTH                         = USERNAME_CHAR_LENGTH * SYSTEM_CHARSET_MBMAXLEN;

    public static final String MYSQL_AUTODETECT_CHARSET_NAME        = "auto";

    public static final int SERVER_VERSION_LENGTH                   = 60;
    public static final int SQLSTATE_LENGTH                         = 5;

    public static final int TABLE_COMMENT_INLINE_MAXLEN             = 180;
    public static final int TABLE_COMMENT_MAXLEN                    = 2048;
    public static final int COLUMN_COMMENT_MAXLEN                   = 1024;
    public static final int INDEX_COMMENT_MAXLEN                    = 1024;
    public static final int TABLE_PARTITION_COMMENT_MAXLEN          = 1024;

    public static final int USER_HOST_BUFF_SIZE                     = HOSTNAME_LENGTH + USERNAME_LENGTH + 2;

    public static final int MYSQL_ERRMSG_SIZE                       = 512;
    public static final int NET_READ_TIMEOUT                        = 30;
    public static final int NET_WRITE_TIMEOUT                       = 60;
    public static final int NET_WAIT_TIMEOUT                        = 8 * 60 * 60;

    public static final int ONLY_KILL_QUERY                         = 1;

    public static final String LOCAL_HOST                           = "localhost";
    public static final String LOCAL_HOST_NAMEDPIPE                 = ".";

    public static final int MODE_INIT                               = 0;  // Connection opened
    public static final int MODE_READ_HANDSHAKE                     = 1;  // Read the handshake from the server and process it
    public static final int MODE_SEND_HANDSHAKE                     = 2;  // Forward the handshake from the server
    public static final int MODE_READ_AUTH                          = 3;  // Read the reply from the client and process it
    public static final int MODE_SEND_AUTH                          = 4;  // Forward the reply from the client
    public static final int MODE_READ_AUTH_RESULT                   = 5;  // Read the reply from the server and process it
    public static final int MODE_SEND_AUTH_RESULT                   = 6;  // Forward the reply from the server
    public static final int MODE_READ_QUERY                         = 7;  // Read the query from the client and process it
    public static final int MODE_SEND_QUERY                         = 8;  // Send the query to the server
    public static final int MODE_READ_QUERY_RESULT                  = 9;  // Read the result set from the server and and process it
    public static final int MODE_SEND_QUERY_RESULT                  = 10; // Send a result set to the client
    public static final int MODE_CLEANUP                            = 11; // Connection closed

    // Packet types
    public static final byte COM_SLEEP                              = (byte)0x00; // deprecated
    public static final byte COM_QUIT                               = (byte)0x01;
    public static final byte COM_INIT_DB                            = (byte)0x02;
    public static final byte COM_QUERY                              = (byte)0x03;
    public static final byte COM_FIELD_LIST                         = (byte)0x04;
    public static final byte COM_CREATE_DB                          = (byte)0x05;
    public static final byte COM_DROP_DB                            = (byte)0x06;
    public static final byte COM_REFRESH                            = (byte)0x07;
    public static final byte COM_SHUTDOWN                           = (byte)0x08;
    public static final byte COM_STATISTICS                         = (byte)0x09;
    public static final byte COM_PROCESS_INFO                       = (byte)0x0a; // deprecated
    public static final byte COM_CONNECT                            = (byte)0x0b; // deprecated
    public static final byte COM_PROCESS_KILL                       = (byte)0x0c;
    public static final byte COM_DEBUG                              = (byte)0x0d;
    public static final byte COM_PING                               = (byte)0x0e;
    public static final byte COM_TIME                               = (byte)0x0f; // deprecated
    public static final byte COM_DELAYED_INSERT                     = (byte)0x10; // deprecated
    public static final byte COM_CHANGE_USER                        = (byte)0x11;
    public static final byte COM_BINLOG_DUMP                        = (byte)0x12;
    public static final byte COM_TABLE_DUMP                         = (byte)0x13;
    public static final byte COM_CONNECT_OUT                        = (byte)0x14;
    public static final byte COM_REGISTER_SLAVE                     = (byte)0x15;
    public static final byte COM_STMT_PREPARE                       = (byte)0x16;
    public static final byte COM_STMT_EXECUTE                       = (byte)0x17;
    public static final byte COM_STMT_SEND_LONG_DATA                = (byte)0x18;
    public static final byte COM_STMT_CLOSE                         = (byte)0x19;
    public static final byte COM_STMT_RESET                         = (byte)0x1a;
    public static final byte COM_SET_OPTION                         = (byte)0x1b;
    public static final byte COM_STMT_FETCH                         = (byte)0x1c;
    public static final byte COM_DAEMON                             = (byte)0x1d; // deprecated
    public static final byte COM_BINLOG_DUMP_GTID                   = (byte)0x1e;
    public static final byte COM_END                                = (byte)0x1f; // Must be last

    public static final byte OK                                     = (byte)0x00;
    public static final byte ERR                                    = (byte)0xff;
    public static final byte EOF                                    = (byte)0xfe;
    public static final byte LOCAL_INFILE                           = (byte)0xfb;

    public static final int SERVER_STATUS_IN_TRANS                  = 1;
    public static final int SERVER_STATUS_AUTOCOMMIT                = 2;
    public static final int SERVER_MORE_RESULTS_EXISTS              = 8;
    public static final int SERVER_QUERY_NO_GOOD_INDEX_USED         = 16;
    public static final int SERVER_STATUS_NO_GOOD_INDEX_USED        = SERVER_QUERY_NO_GOOD_INDEX_USED;
    public static final int SERVER_QUERY_NO_INDEX_USED              = 32;
    public static final int SERVER_STATUS_NO_INDEX_USED             = SERVER_QUERY_NO_INDEX_USED;
    public static final int SERVER_STATUS_CURSOR_EXISTS             = 64;
    public static final int SERVER_STATUS_LAST_ROW_SENT             = 128;
    public static final int SERVER_STATUS_DB_DROPPED                = 256;
    public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES      = 512;
    public static final int SERVER_STATUS_METADATA_CHANGED          = 1024;
    public static final int SERVER_QUERY_WAS_SLOW                   = 2048;
    public static final int SERVER_PS_OUT_PARAMS                    = 4096;
    public static final int SERVER_STATUS_IN_TRANS_READONLY         = 8192;

    public static final int SERVER_STATUS_CLEAR_SET                 = (
        SERVER_QUERY_NO_GOOD_INDEX_USED
        | SERVER_QUERY_NO_INDEX_USED
        | SERVER_MORE_RESULTS_EXISTS
        | SERVER_STATUS_METADATA_CHANGED
        | SERVER_QUERY_WAS_SLOW
        | SERVER_STATUS_DB_DROPPED
        | SERVER_STATUS_CURSOR_EXISTS
        | SERVER_STATUS_LAST_ROW_SENT
    );

    public static final int CLIENT_LONG_PASSWORD                    = 1;
    public static final int CLIENT_FOUND_ROWS                       = 2;
    public static final int CLIENT_LONG_FLAG                        = 4;
    public static final int CLIENT_CONNECT_WITH_DB                  = 8;
    public static final int CLIENT_NO_SCHEMA                        = 16;
    public static final int CLIENT_COMPRESS                         = 32;
    public static final int CLIENT_ODBC                             = 64;
    public static final int CLIENT_LOCAL_FILES                      = 128;
    public static final int CLIENT_IGNORE_SPACE                     = 256;
    public static final int CLIENT_PROTOCOL_41                      = 512;
    public static final int CLIENT_INTERACTIVE                      = 1024;
    public static final int CLIENT_SSL                              = 2048;
    public static final int CLIENT_IGNORE_SIGPIPE                   = 4096;
    public static final int CLIENT_TRANSACTIONS                     = 8192;
    public static final int CLIENT_RESERVED                         = 16384;
    public static final int CLIENT_SECURE_CONNECTION                = 32768;
    public static final int CLIENT_MULTI_STATEMENTS                 = 1 << 16;
    public static final int CLIENT_MULTI_RESULTS                    = 1 << 17;
    public static final int CLIENT_PS_MULTI_RESULTS                 = 1 << 18;
    public static final int CLIENT_PLUGIN_AUTH                      = 1 << 19;
    public static final int CLIENT_CONNECT_ATTRS                    = 1 << 20;
    public static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA   = 1 << 21;
    public static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS     = 1 << 22;
    public static final int CLIENT_SSL_VERIFY_SERVER_CERT           = 1 << 30;
    public static final int CLIENT_REMEMBER_OPTIONS                 = 1 << 31;

    public static final int CAN_CLIENT_COMPRESS                     = 0;

    public static final int CLIENT_ALL_FLAGS                        = (
        CLIENT_LONG_PASSWORD
        | CLIENT_FOUND_ROWS
        | CLIENT_LONG_FLAG
        | CLIENT_CONNECT_WITH_DB
        | CLIENT_NO_SCHEMA
        | CLIENT_COMPRESS
        | CLIENT_ODBC
        | CLIENT_LOCAL_FILES
        | CLIENT_IGNORE_SPACE
        | CLIENT_PROTOCOL_41
        | CLIENT_INTERACTIVE
        | CLIENT_SSL
        | CLIENT_IGNORE_SIGPIPE
        | CLIENT_TRANSACTIONS
        | CLIENT_RESERVED
        | CLIENT_SECURE_CONNECTION
        | CLIENT_MULTI_STATEMENTS
        | CLIENT_MULTI_RESULTS
        | CLIENT_PS_MULTI_RESULTS
        | CLIENT_SSL_VERIFY_SERVER_CERT
        | CLIENT_REMEMBER_OPTIONS
        | CLIENT_PLUGIN_AUTH
        | CLIENT_CONNECT_ATTRS
        | CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA
        | CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS
    );

    public static final int CLIENT_BASIC_FLAGS                      =
    (((CLIENT_ALL_FLAGS & ~CLIENT_SSL)
                        & ~CLIENT_COMPRESS)
                        & ~CLIENT_SSL_VERIFY_SERVER_CERT);

    public static final int MYSQL_TYPE_DECIMAL                      = 0;
    public static final int MYSQL_TYPE_TINY                         = 1;
    public static final int MYSQL_TYPE_SHORT                        = 2;
    public static final int MYSQL_TYPE_LONG                         = 3;
    public static final int MYSQL_TYPE_FLOAT                        = 4;
    public static final int MYSQL_TYPE_DOUBLE                       = 5;
    public static final int MYSQL_TYPE_NULL                         = 6;
    public static final int MYSQL_TYPE_TIMESTAMP                    = 7;
    public static final int MYSQL_TYPE_LONGLONG                     = 8;
    public static final int MYSQL_TYPE_INT24                        = 9;
    public static final int MYSQL_TYPE_DATE                         = 10;
    public static final int MYSQL_TYPE_TIME                         = 11;
    public static final int MYSQL_TYPE_DATETIME                     = 12;
    public static final int MYSQL_TYPE_YEAR                         = 13;
    public static final int MYSQL_TYPE_NEWDATE                      = 14;
    public static final int MYSQL_TYPE_VARCHAR                      = 15;
    public static final int MYSQL_TYPE_BIT                          = 16;
    public static final int MYSQL_TYPE_TIMESTAMP2                   = 17;
    public static final int MYSQL_TYPE_DATETIME2                    = 18;
    public static final int MYSQL_TYPE_TIME2                        = 19;
    public static final int MYSQL_TYPE_NEWDECIMAL                   = 246;
    public static final int MYSQL_TYPE_ENUM                         = 247;
    public static final int MYSQL_TYPE_SET                          = 248;
    public static final int MYSQL_TYPE_TINY_BLOB                    = 249;
    public static final int MYSQL_TYPE_MEDIUM_BLOB                  = 250;
    public static final int MYSQL_TYPE_LONG_BLOB                    = 251;
    public static final int MYSQL_TYPE_BLOB                         = 252;
    public static final int MYSQL_TYPE_VAR_STRING                   = 253;
    public static final int MYSQL_TYPE_STRING                       = 254;
    public static final int MYSQL_TYPE_GEOMETRY                     = 255;

    public static final int REFRESH_GRANT                           = 1;
    public static final int REFRESH_LOG                             = 2;
    public static final int REFRESH_TABLES                          = 4;
    public static final int REFRESH_HOSTS                           = 8;
    public static final int REFRESH_STATUS                          = 16;
    public static final int REFRESH_THREADS                         = 32;
    public static final int REFRESH_SLAVE                           = 64;
    public static final int REFRESH_MASTER                          = 128;
    public static final int REFRESH_ERROR_LOG                       = 256;
    public static final int REFRESH_ENGINE_LOG                      = 512;
    public static final int REFRESH_BINARY_LOG                      = 1024;
    public static final int REFRESH_RELAY_LOG                       = 2048;
    public static final int REFRESH_GENERAL_LOG                     = 4096;
    public static final int REFRESH_SLOW_LOG                        = 8192;
    public static final int REFRESH_READ_LOCK                       = 16384;
    public static final int REFRESH_FAST                            = 32768;
    public static final int REFRESH_QUERY_CACHE                     = 65536;
    public static final int REFRESH_QUERY_CACHE_FREE                = 0x20000;
    public static final int REFRESH_DES_KEY_FILE                    = 0x40000;
    public static final int REFRESH_USER_RESOURCES                  = 0x80000;
    public static final int REFRESH_FOR_EXPORT                      = 0x100000;

    public static final int CURSOR_TYPE_NO_CURSOR                   = 0;
    public static final int CURSOR_TYPE_READ_ONLY                   = 1;
    public static final int CURSOR_TYPE_FOR_UPDATE                  = 2;
    public static final int CURSOR_TYPE_SCROLLABLE                  = 4;

    public static final int MYSQL_OPTION_MULTI_STATEMENTS_ON        = 0;
    public static final int MYSQL_OPTION_MULTI_STATEMENTS_OFF       = 1;

    public static final int ROW_TYPE_TEXT                           = 0;
    public static final int ROW_TYPE_BINARY                         = 1;

    public static final int RS_OK                                   = 0;
    public static final int RS_FULL                                 = 1;
    public static final int RS_COL_DEF                              = 2;
    public static final int RS_DATA_FILE                            = 3;

    public static final int SCRAMBLE_LENGTH                         = 20;
    public static final int SCRAMBLE_LENGTH_323                     = 8;
    public static final int SCRAMBLED_PASSWORD_CHAR_LENGTH          = SCRAMBLE_LENGTH * 2 + 1;
    public static final int SCRAMBLED_PASSWORD_CHAR_LENGTH_323      = SCRAMBLE_LENGTH_323 * 2;

    public static final int NOT_NULL_FLAG                           = 1;
    public static final int PRI_KEY_FLAG                            = 2;
    public static final int UNIQUE_KEY_FLAG                         = 4;
    public static final int MULTIPLE_KEY_FLAG                       = 8;
    public static final int BLOB_FLAG                               = 16;
    public static final int UNSIGNED_FLAG                           = 32;
    public static final int ZEROFILL_FLAG                           = 64;
    public static final int BINARY_FLAG                             = 128;

    public static final int ENUM_FLAG                               = 256;
    public static final int AUTO_INCREMENT_FLAG                     = 512;
    public static final int TIMESTAMP_FLAG                          = 1024;
    public static final int SET_FLAG                                = 2048;
    public static final int NO_DEFAULT_VALUE_FLAG                   = 4096;
    public static final int ON_UPDATE_NOW_FLAG                      = 8192;
    public static final int NUM_FLAG                                = 32768;
    public static final int PART_KEY_FLAG                           = 16384;
    public static final int GROUP_FLAG                              = 32768;
    public static final int UNIQUE_FLAG                             = 65536;
    public static final int BINCMP_FLAG                             = 131072;
    public static final int GET_FIXED_FIELDS_FLAG                   = 1 << 18;
    public static final int FIELD_IN_PART_FUNC_FLAG                 = 1 << 19;

    public static final int FIELD_IN_ADD_INDEX                      = 1 << 20;
    public static final int FIELD_IS_RENAMED                        = 1 << 21;
    public static final int FIELD_FLAGS_STORAGE_MEDIA               = 22;
    public static final int FIELD_FLAGS_STORAGE_MEDIA_MASK          = 3 << FIELD_FLAGS_STORAGE_MEDIA;
    public static final int FIELD_FLAGS_COLUMN_FORMAT               = 24;
    public static final int FIELD_FLAGS_COLUMN_FORMAT_MASK          = 3 << FIELD_FLAGS_COLUMN_FORMAT;
    public static final int FIELD_IS_DROPPED                        = 1<< 26;

    public static final int MAX_TINYINT_WIDTH                       = 3;
    public static final int MAX_SMALLINT_WIDTH                      = 5;
    public static final int MAX_MEDIUMINT_WIDTH                     = 8;
    public static final int MAX_INT_WIDTH                           = 10;
    public static final int MAX_BIGINT_WIDTH                        = 20;
    public static final int MAX_CHAR_WIDTH                          = 255;
    public static final int MAX_BLOB_WIDTH                          = 16777216;

    public static final int packet_error                            = 0;

    public static final int MYSQL_SHUTDOWN_KILLABLE_CONNECT         = (char)(1 << 0);
    public static final int MYSQL_SHUTDOWN_KILLABLE_TRANS           = (char)(1 << 1);
    public static final int MYSQL_SHUTDOWN_KILLABLE_LOCK_TABLE      = (char)(1 << 2);
    public static final int MYSQL_SHUTDOWN_KILLABLE_UPDATE          = (char)(1 << 3);

    public static final int SHUTDOWN_DEFAULT                        = 0;
    public static final int SHUTDOWN_WAIT_CONNECTIONS               = MYSQL_SHUTDOWN_KILLABLE_CONNECT;
    public static final int SHUTDOWN_WAIT_TRANSACTIONS              = MYSQL_SHUTDOWN_KILLABLE_TRANS;
    public static final int SHUTDOWN_WAIT_UPDATES                   = MYSQL_SHUTDOWN_KILLABLE_UPDATE;
    public static final int SHUTDOWN_WAIT_ALL_BUFFERS               = MYSQL_SHUTDOWN_KILLABLE_UPDATE << 1;
    public static final int SHUTDOWN_WAIT_CRITICAL_BUFFERS          = (MYSQL_SHUTDOWN_KILLABLE_UPDATE << 1) + 1;
    public static final int KILL_QUERY                              = 254;
    public static final int KILL_CONNECTION                         = 255;

    public static final int STRING_RESULT                           = 0;
    public static final int REAL_RESULT                             = 1;
    public static final int INT_RESULT                              = 2;
    public static final int ROW_RESULT                              = 3;
    public static final int DECIMAL_RESULT                          = 4;

    public static final int NET_HEADER_SIZE                         = 4;
    public static final int COMP_HEADER_SIZE                        = 3;

    public static final int NULL_LENGTH                             = ~0;
    public static final int MYSQL_STMT_HEADER                       = 4;
    public static final int MYSQL_LONG_DATA_HEADER                  = 6;

    public static final int NOT_FIXED_DEC                           = 31;

}
