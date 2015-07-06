/**
 * 
 */
package jazmin.driver.jdbc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.core.app.AutoWired;
import jazmin.util.IOUtil;


/**
 * @author yama
 * 27 Dec, 2014
 */
public class JazminDAO {
	@AutoWired
	protected ConnectionDriver connectionDriver;
	//
	public JazminDAO() {	
	}
	//
	/**
	 * @return the connectionDriver
	 */
	public ConnectionDriver getConnectionDriver() {
		return connectionDriver;
	}
	/**
	 * @param connectionDriver the connectionDriver to set
	 */
	public void setConnectionDriver(ConnectionDriver connectionDriver) {
		this.connectionDriver = connectionDriver;
	}
	//
	protected final <T> T queryForObject(
			String sql,
			ResultSetHandler<T> rowHandler, 
			Object... parameters) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			JDBCUtil.set(conn, ps, parameters);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rowHandler.handleRow(rs);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeResultSet(rs);
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
	//
	protected final <T> List<T> queryForList(
			String sql,
			ResultSetHandler<T> rowHandler, 
			Object... parameters) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			JDBCUtil.set(conn, ps, parameters);
			rs = ps.executeQuery();
			List<T> result = new ArrayList<T>();
			while (rs.next()) {
				result.add(rowHandler.handleRow(rs));
			}
			return result;
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeResultSet(rs);
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
	//-------------------------------------------------------------------------
	
	public void getBean(Object o,ResultSet rs,String ...excludes) throws Exception{
		Set<String>excludesNames=new TreeSet<String>();
		for(String e:excludes){
			excludesNames.add(e);
		}
		Class<?>type=o.getClass();
		for(Field f:type.getFields()){
			if(excludesNames.contains(f.getName())){
				continue;
			}
			String fieldName=convertFieldName(f.getName());
			Class<?>fieldType=f.getType();
			if(Modifier.isStatic(f.getModifiers())){
				continue;
			}
			if(f.getAnnotation(FieldIgnore.class)!=null){
				continue;
			}
			Object value=null;
			if(fieldType.equals(String.class)){
				value=rs.getString(fieldName);
			}else if(fieldType.equals(Integer.class)||fieldType.equals(int.class)){
				value=rs.getInt(fieldName);
			}else if(fieldType.equals(Short.class)||fieldType.equals(short.class)){
				value=rs.getShort(fieldName);
			}else if(fieldType.equals(Long.class)||fieldType.equals(long.class)){
				value=rs.getLong(fieldName);
			}else if(fieldType.equals(Double.class)||fieldType.equals(double.class)){
				value=rs.getDouble(fieldName);
			}else if(fieldType.equals(Float.class)||fieldType.equals(float.class)){
				value=rs.getFloat(fieldName);
			}else if(fieldType.equals(Date.class)){
				value=rs.getTimestamp(fieldName);
			}else if(fieldType.equals(Boolean.class)||fieldType.equals(boolean.class)){
				value=rs.getBoolean(fieldName);
			}else if(fieldType.equals(BigDecimal.class)){
				value=rs.getBigDecimal(fieldName);
			}else if(fieldType.equals(byte[].class)){
				Blob bb=rs.getBlob(fieldName);
				if(bb!=null){
					ByteArrayOutputStream bos=new ByteArrayOutputStream();
					IOUtil.copy(bb.getBinaryStream(), bos);
					value=bos.toByteArray();			
				}
			}else{
				throw new IllegalArgumentException("bad field type:"+fieldName+"/"+fieldType);
			}
			f.setAccessible(true);
			if(value!=null){
				f.set(o, value);			
			}
		}
	}
	//
	private static String convertFieldName(String name){
		StringBuffer result=new StringBuffer();
		for(char c:name.toCharArray()){
			if(Character.isUpperCase(c)){
				result.append("_");
			}
			result.append(Character.toLowerCase(c));		
		}
		return result.toString();
	}
	//-------------------------------------------------------------------------
	//
	protected final Boolean queryForBoolean(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getBoolean(1), parameters);
	}
	//
	protected final String queryForString(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getString(1), parameters);
	}
	//
	protected final Double queryForDouble(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getDouble(1), parameters);
	}
	//
	protected final Float queryForFloat(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getFloat(1), parameters);
	}
	//
	protected final Integer queryForInteger(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getInt(1), parameters);
	}
	//
	protected final Short queryForShort(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getShort(1), parameters);
		
	}
	//
	protected final BigDecimal queryForBigDecimal(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getBigDecimal(1), parameters);
		
	}
	//
	protected final Byte queryForByte(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getByte(1), parameters);
		
	}
	//
	protected final  Date queryForDate(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getTimestamp(1), parameters);
	}
	//
	protected final boolean execute(
			String sql,
			Object... parameters) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			JDBCUtil.set(conn, ps, parameters);
			return ps.execute();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
	//
	/**
	 * @param sql String
	 * @param parameters Object[]
	 * @return boolean
	 */
	protected final int executeWithGenKey(
			String sql,
			Object... parameters) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			JDBCUtil.set(conn, ps, parameters);
			ps.execute();
			rs=ps.getGeneratedKeys();
			if(rs.next()){
				return rs.getInt(1);
			}else{
				throw new ConnectionException("can not got generatedKeys");
			}
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeResultSet(rs);
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
	//
	protected final int executeUpdate(
			String sql,
			Object... parameters) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			JDBCUtil.set(conn, ps, parameters);
			return ps.executeUpdate();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
	//
	protected final int[] executeBatch(
			String sql,
			List<Object[]> parameterList) {
		Connection conn = connectionDriver.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			for(Object []parameters:parameterList){
				JDBCUtil.set(conn, ps, parameters);
				ps.addBatch();
			}
			return ps.executeBatch();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			JDBCUtil.closeStatement(ps);
			JDBCUtil.closeConnection(conn);
		}
	}
}
