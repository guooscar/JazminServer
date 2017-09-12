/**
 * 
 */
package jazmin.driver.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jazmin.core.app.AutoWired;


/**
 * @author yama
 * 27 Dec, 2014
 */
public class JazminDAO {
	protected int limitMaxRows=-1;
	@AutoWired
	protected ConnectionDriver connectionDriver;
	//
	public JazminDAO() {	
	}
	
	/**
	 * @return the limitMaxRow
	 */
	public int getLimitMaxRows() {
		return limitMaxRows;
	}

	/**
	 * @param limitMaxRow the limitMaxRow to set
	 */
	public void setLimitMaxRows(int limitMaxRow) {
		this.limitMaxRows = limitMaxRow;
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
			if(limitMaxRows>0){
				ps.setMaxRows(limitMaxRows);		
			}
			ConnectionUtil.set(ps, parameters);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rowHandler.handleRow(rs);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			ConnectionUtil.closeResultSet(rs);
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
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
			if(limitMaxRows>0){
				ps.setMaxRows(limitMaxRows);		
			}
			ConnectionUtil.set(ps, parameters);
			rs = ps.executeQuery();
			List<T> result = new ArrayList<T>();
			while (rs.next()) {
				result.add(rowHandler.handleRow(rs));
			}
			return result;
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			ConnectionUtil.closeResultSet(rs);
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
		}
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
	protected final Long queryForLong(String sql,Object ...parameters){
		return  queryForObject(sql,rs->rs.getLong(1), parameters);
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
			ConnectionUtil.set( ps, parameters);
			return ps.execute();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
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
			ConnectionUtil.set( ps, parameters);
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
			ConnectionUtil.closeResultSet(rs);
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
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
			ConnectionUtil.set(ps, parameters);
			return ps.executeUpdate();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
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
				ConnectionUtil.set(ps, parameters);
				ps.addBatch();
			}
			return ps.executeBatch();
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			ConnectionUtil.closeStatement(ps);
			ConnectionUtil.closeConnection(conn);
		}
	}
}
