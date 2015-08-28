/**
 * 
 */
package jazmin.deploy.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;

/**
 * @author yama
 *
 */
public class JdbcUtil {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	//
	public static boolean execute(String dburl, 
			String user, 
			String pwd,
			String sql) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			stmt = conn.createStatement();
			boolean r=stmt.execute(sql);
			stmt.close();
			conn.close();
			return r;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				se2.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	//
	public static int executeUpdate(String dburl, 
			String user, 
			String pwd,
			String sql) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			stmt = conn.createStatement();
			int r=stmt.executeUpdate(sql);
			stmt.close();
			conn.close();
			return r;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				se2.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	//
	public static void executeQuery(
			String dburl, 
			String user, 
			String pwd,
			String sql,
			BiConsumer<ResultSetMetaData,ResultSet>callback) throws Exception {
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			stmt = conn.createStatement();
			stmt.setMaxRows(10000);
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metaData=rs.getMetaData();
			callback.accept(metaData, rs);
			rs.close();
			stmt.close();
			conn.close();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				se2.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
}
