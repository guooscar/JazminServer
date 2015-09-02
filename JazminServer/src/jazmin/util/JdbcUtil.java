/**
 * 
 */
package jazmin.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import jazmin.util.DumpUtil;

/**
 * @author yama
 *
 */
public class JdbcUtil {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	//
	public static class TableInfo{
		public String name;
		public String remarks;
	}
	//
	public static class ColumnInfo{
		public String name;
		public String type;
		public int dataSize;
		public String remarks;
		public String columnDef;
		public String isAutoincrement;
		public String nullable;
	}
	//
	public static class IndexInfo{
		public String name;
		public boolean nonUnique;
		public String indexQualifier;
		public String columnName;
		public String type;
	}
	public static class PrimaryKeyInfo{
		public String name;
		public String columnName;
	}
	//
	public static List<ColumnInfo> getColumns(
			String dburl, 
			String user, 
			String pwd,
			String table) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			DatabaseMetaData metaData=conn.getMetaData();
			ResultSet rs =metaData.getColumns(null, null, table, null);
			List<ColumnInfo>r=new ArrayList<JdbcUtil.ColumnInfo>();
			while(rs.next()){
				ColumnInfo ti = new ColumnInfo();
				ti.name = rs.getString("COLUMN_NAME"); // 列名
				ti.type = rs.getString("TYPE_NAME"); // java.sql.Types类型名称(列类型名称)
				ti.dataSize = rs.getInt("COLUMN_SIZE"); // 列大小
				ti.remarks = rs.getString("REMARKS"); // 列描述
				ti.columnDef = rs.getString("COLUMN_DEF"); // 默认值
				ti.nullable = rs.getString("IS_NULLABLE");
				ti.isAutoincrement = rs.getString("IS_AUTOINCREMENT");
				r.add(ti);
			}
			conn.close();
			return r;
		} finally {
		
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	//
	public static List<PrimaryKeyInfo> getPrimaryKeys(
			String dburl, 
			String user, 
			String pwd,
			String table) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			DatabaseMetaData metaData=conn.getMetaData();
			ResultSet rs =metaData.getPrimaryKeys(null, null, table);
			List<PrimaryKeyInfo>r=new ArrayList<JdbcUtil.PrimaryKeyInfo>();
			while(rs.next()){
				PrimaryKeyInfo ti = new PrimaryKeyInfo();
				ti.name = rs.getString("PK_NAME"); // 列名
				ti.columnName=rs.getString("COLUMN_NAME");
				r.add(ti);
			}
			conn.close();
			return r;
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	//
	public static List<IndexInfo> getIndexs(
			String dburl, 
			String user, 
			String pwd,
			String table) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			DatabaseMetaData metaData=conn.getMetaData();
			ResultSet rs =metaData.getIndexInfo(null, null, table, false, true);
			List<IndexInfo>r=new ArrayList<JdbcUtil.IndexInfo>();
			while(rs.next()){
				IndexInfo ti = new IndexInfo();
				ti.name = rs.getString("INDEX_NAME"); // 列名
				int type= rs.getShort("TYPE");
				if(type==DatabaseMetaData.tableIndexClustered){
					ti.type ="tableIndexClustered";
				}
				if(type==DatabaseMetaData.tableIndexStatistic){
					ti.type ="tableIndexStatistic";
				}
				if(type==DatabaseMetaData.tableIndexHashed){
					ti.type ="tableIndexHashed";
				}
				if(type==DatabaseMetaData.tableIndexOther){
					ti.type ="tableIndexOther";
				}
				ti.nonUnique=rs.getBoolean("NON_UNIQUE");
				ti.columnName=rs.getString("COLUMN_NAME");
				ti.indexQualifier=rs.getString("INDEX_QUALIFIER");
				r.add(ti);
			}
			conn.close();
			return r;
		} finally {
			
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	//
	public static List<TableInfo> getTables(
			String dburl, 
			String user, 
			String pwd) throws Exception{
		Class.forName(JDBC_DRIVER);
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dburl, user, pwd);
			DatabaseMetaData metaData=conn.getMetaData();
			ResultSet tableRet =metaData.getTables(null, "%","%",new String[]{"TABLE"});
			List<TableInfo>r=new ArrayList<JdbcUtil.TableInfo>();
			while(tableRet.next()){
				TableInfo ti=new TableInfo();
				ti.name=(tableRet.getString("TABLE_NAME"));
				ti.remarks=(tableRet.getString("REMARKS"));
				r.add(ti);
			} 
			conn.close();
			return r;
		} finally {
			
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
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
	//
	public static void main(String[] args)throws Exception{
		String jdbcUrl="jdbc:mysql://192.168.0.12:3306/db_fundingbiz_dev?useUnicode=true&characterEncoding=UTF-8";
		List<TableInfo>l=JdbcUtil.getTables(jdbcUrl,"root", "kp123456");
		l.forEach(t->{
			System.out.println(DumpUtil.dump(t));
			//
			try {
				System.out.println(DumpUtil.dump(JdbcUtil.getColumns(jdbcUrl,"root", "kp123456",t.name)));
				System.out.println(DumpUtil.dump(JdbcUtil.getIndexs(jdbcUrl,"root", "kp123456",t.name)));
				System.out.println(DumpUtil.dump(JdbcUtil.getPrimaryKeys(jdbcUrl,"root", "kp123456",t.name)));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
