/**
 * 
 */
package jazmin.driver.jdbc;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import jazmin.core.Jazmin;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * C3P0ConnectionDriver is wrapper of C3P0 JDBC connection pool.for more information
 * please visit http://www.mchange.com/projects/c3p0/
 * @author yama
 * 27 Dec, 2014
 */
public class C3p0ConnectionDriver extends ConnectionDriver {
	private ComboPooledDataSource  dataSource;
	//
	public C3p0ConnectionDriver() {
		dataSource=new ComboPooledDataSource();
		dataSource.setMaxConnectionAge(3600);
	}
	//
	@Override
	public Connection getWorkConnection() throws SQLException {
		return dataSource.getConnection();
	}
	/**
	 * return data source checkout connection timeout time
	 * @return data source checkout connection timeout time
	 */
	public int getCheckoutTimeout() {
		return dataSource.getCheckoutTimeout();
	}
	/**
	 * return jdbc driver class name
	 * @return jdbc driver class name
	 */
	public String getDriverClass() {
		return dataSource.getDriverClass();
	}
	/**
	 * return initial connection pool size
	 * @return initial connection pool size
	 */
	public int getInitialPoolSize() {
		return dataSource.getInitialPoolSize();
	}
	/**
	 * return jdbc connection url
	 * @return jdbc connection url
	 */
	public String getUrl() {
		return dataSource.getJdbcUrl();
	}
	/**
	 * return jdbc login timeout time
	 * @return jdbc login timeout time
	 */
	public int getLoginTimeout()  {
		try {
			return dataSource.getLoginTimeout();
		} catch (SQLException e) {
			return 0;
		}
	}
	/**
	 * return max jdbc connection alive time
	 * @return  max jdbc connection alive time
	 */
	public int getMaxConnectionAge() {
		return dataSource.getMaxConnectionAge();
	}
	/**
	 * return jdbc max idle time
	 * @return jdbc max idle time
	 */
	public int getMaxIdleTime() {
		return dataSource.getMaxIdleTime();
	}
	/**
	 * return max jdbc connection pool size
	 * @return
	 */
	public int getMaxPoolSize() {
		return dataSource.getMaxPoolSize();
	}
	/**
	 * return min jdbc connection pool size
	 * @return
	 */
	public int getMinPoolSize() {
		return dataSource.getMinPoolSize();
	}
	/**
	 * return current jdbc connection number
	 * @return current jdbc connection number
	 * @throws SQLException
	 */
	public int getNumConnections() throws SQLException {
		return dataSource.getNumConnections();
	}
	/**
	 * return current jdbc idle connection number
	 * @return  current jdbc idle connection number
	 * @throws SQLException
	 */
	public int getNumIdleConnections() throws SQLException {
		return dataSource.getNumIdleConnections();
	}
	/**
	 * return thread pool active threads number
	 * @return thread pool active threads number
	 * @throws SQLException
	 */
	public int getThreadPoolNumActiveThreads() throws SQLException {
		return dataSource.getThreadPoolNumActiveThreads();
	}
	/**
	 * return thread pool idle thread number
	 * @return thread pool idle thread number
	 * @throws SQLException
	 */
	public int getThreadPoolNumIdleThreads() throws SQLException {
		return dataSource.getThreadPoolNumIdleThreads();
	}
	/**
	 * return thread pool pending task number 
	 * @return thread pool pending task number 
	 * @throws SQLException
	 */
	public int getThreadPoolNumTasksPending() throws SQLException {
		return dataSource.getThreadPoolNumTasksPending();
	}
	/**
	 * return thread pool size 
	 * @return thread pool size
	 */
	public int getThreadPoolSize() {
		try {
			return dataSource.getThreadPoolSize();
		} catch (SQLException e) {
			return 0;
		}
	}
	/**
	 * return jdbc user
	 * @return jdbc user
	 */
	public String getUser() {
		return dataSource.getUser();
	}
	/**
	 * return auto commit flag when close connection 
	 * @return   auto commit flag when close connection 
	 */
	public boolean isAutoCommitOnClose() {
		return dataSource.isAutoCommitOnClose();
	}
	/**
	 * set checkout timeout time
	 * @param checkoutTimeout
	 */
	public void setCheckoutTimeout(int checkoutTimeout) {
		dataSource.setCheckoutTimeout(checkoutTimeout);
	}
	/**
	 * set jdbc driver class name
	 * @param driverClass jdbc driver class name
	 * @throws PropertyVetoException
	 */
	public void setDriverClass(String driverClass) throws PropertyVetoException {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setDriverClass(driverClass);
	}
	/**
	 * set initial jdbc connection pool size
	 * @param initialPoolSize initial jdbc connection pool size
	 */
	public void setInitialPoolSize(int initialPoolSize) {
		
		dataSource.setInitialPoolSize(initialPoolSize);
	}
	/**
	 * set jdbc connection url
	 * @param jdbcUrl jdbc connection url
	 */
	public void setUrl(String jdbcUrl) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setJdbcUrl(jdbcUrl);
	}
	/**
	 * set jdbc connection login timeout time
	 * @param seconds jdbc connection login timeout time in seconds
	 * @throws SQLException
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		
		dataSource.setLoginTimeout(seconds);
	}
	/**
	 * set max connection alive time
	 * @param maxConnectionAge max connection alive time
	 */
	public void setMaxConnectionAge(int maxConnectionAge) {
		
		dataSource.setMaxConnectionAge(maxConnectionAge);
	}
	/**
	 * set max connection idle time
	 * @param maxIdleTime  max connection idle time
	 */
	public void setMaxIdleTime(int maxIdleTime) {
		
		dataSource.setMaxIdleTime(maxIdleTime);
	}
	/**
	 * set max connection pool size
	 * @param maxPoolSize  max connection pool size
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		
		dataSource.setMaxPoolSize(maxPoolSize);
	}
	/**
	 * set max statements count
	 * @param maxStatements
	 */
	public void setMaxStatements(int maxStatements) {
	
		dataSource.setMaxStatements(maxStatements);
	}
	/**
	 * set min pool size
	 * @param minPoolSize
	 */
	public void setMinPoolSize(int minPoolSize) {
		
		dataSource.setMinPoolSize(minPoolSize);
	}
	/**
	 * set jdbc password
	 * @param password jdbc password
	 */
	public void setPassword(String password) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setPassword(password);
	}
	/**
	 * set jdbc user
	 * @param user jdbc user
	 */
	public void setUser(String user) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setUser(user);
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		super.init();
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(C3p0DriverCommand.class);
		}
	}
	//
	@Override
	public void stop() throws Exception {
		super.stop();
		DataSources.destroy(dataSource);
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
				.print("user",getUser())
				.print("driverClass",getDriverClass())
				.print("initialPoolSize",getInitialPoolSize())
				.print("url",getUrl())
				.print("loginTimeout",getLoginTimeout())
				.print("maxConnectionAge",getMaxConnectionAge())
				.print("maxIdleTime",getMaxIdleTime())
				.print("maxPoolSize",getMaxPoolSize())
				.print("minPoolSize",getMinPoolSize())
				.print("threadPoolSize",getThreadPoolSize())
				.print("statSql",isStatSql())
				.toString();
	}
}
