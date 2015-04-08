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
public class C3P0ConnectionDriver extends ConnectionDriver {
	private ComboPooledDataSource  dataSource;
	//
	public C3P0ConnectionDriver() {
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
	public int checkoutTimeout() {
		return dataSource.getCheckoutTimeout();
	}
	/**
	 * return jdbc driver class name
	 * @return jdbc driver class name
	 */
	public String driverClass() {
		return dataSource.getDriverClass();
	}
	/**
	 * return initial connection pool size
	 * @return initial connection pool size
	 */
	public int initialPoolSize() {
		return dataSource.getInitialPoolSize();
	}
	/**
	 * return jdbc connection url
	 * @return jdbc connection url
	 */
	public String url() {
		return dataSource.getJdbcUrl();
	}
	/**
	 * return jdbc login timeout time
	 * @return jdbc login timeout time
	 */
	public int loginTimeout()  {
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
	public int maxConnectionAge() {
		return dataSource.getMaxConnectionAge();
	}
	/**
	 * return jdbc max idle time
	 * @return jdbc max idle time
	 */
	public int maxIdleTime() {
		return dataSource.getMaxIdleTime();
	}
	/**
	 * return max jdbc connection pool size
	 * @return
	 */
	public int maxPoolSize() {
		return dataSource.getMaxPoolSize();
	}
	/**
	 * return min jdbc connection pool size
	 * @return
	 */
	public int minPoolSize() {
		return dataSource.getMinPoolSize();
	}
	/**
	 * return current jdbc connection number
	 * @return current jdbc connection number
	 * @throws SQLException
	 */
	public int numConnections() throws SQLException {
		return dataSource.getNumConnections();
	}
	/**
	 * return current jdbc idle connection number
	 * @return  current jdbc idle connection number
	 * @throws SQLException
	 */
	public int numIdleConnections() throws SQLException {
		return dataSource.getNumIdleConnections();
	}
	/**
	 * return thread pool active threads number
	 * @return thread pool active threads number
	 * @throws SQLException
	 */
	public int threadPoolNumActiveThreads() throws SQLException {
		return dataSource.getThreadPoolNumActiveThreads();
	}
	/**
	 * return thread pool idle thread number
	 * @return thread pool idle thread number
	 * @throws SQLException
	 */
	public int threadPoolNumIdleThreads() throws SQLException {
		return dataSource.getThreadPoolNumIdleThreads();
	}
	/**
	 * return thread pool pending task number 
	 * @return thread pool pending task number 
	 * @throws SQLException
	 */
	public int threadPoolNumTasksPending() throws SQLException {
		return dataSource.getThreadPoolNumTasksPending();
	}
	/**
	 * return thread pool size 
	 * @return thread pool size
	 */
	public int threadPoolSize() {
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
	public String user() {
		return dataSource.getUser();
	}
	/**
	 * return auto commit flag when close connection 
	 * @return   auto commit flag when close connection 
	 */
	public boolean autoCommitOnClose() {
		return dataSource.isAutoCommitOnClose();
	}
	/**
	 * set checkout timeout time
	 * @param checkoutTimeout
	 */
	public void checkoutTimeout(int checkoutTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setCheckoutTimeout(checkoutTimeout);
	}
	/**
	 * set jdbc driver class name
	 * @param driverClass jdbc driver class name
	 * @throws PropertyVetoException
	 */
	public void driverClass(String driverClass) throws PropertyVetoException {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setDriverClass(driverClass);
	}
	/**
	 * set initial jdbc connection pool size
	 * @param initialPoolSize initial jdbc connection pool size
	 */
	public void initialPoolSize(int initialPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setInitialPoolSize(initialPoolSize);
	}
	/**
	 * set jdbc connection url
	 * @param jdbcUrl jdbc connection url
	 */
	public void url(String jdbcUrl) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setJdbcUrl(jdbcUrl);
	}
	/**
	 * set jdbc connection login timeout time
	 * @param seconds jdbc connection login timeout time in seconds
	 * @throws SQLException
	 */
	public void loginTimeout(int seconds) throws SQLException {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setLoginTimeout(seconds);
	}
	/**
	 * set max connection alive time
	 * @param maxConnectionAge max connection alive time
	 */
	public void maxConnectionAge(int maxConnectionAge) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxConnectionAge(maxConnectionAge);
	}
	/**
	 * set max connection idle time
	 * @param maxIdleTime  max connection idle time
	 */
	public void maxIdleTime(int maxIdleTime) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxIdleTime(maxIdleTime);
	}
	/**
	 * set max connection pool size
	 * @param maxPoolSize  max connection pool size
	 */
	public void maxPoolSize(int maxPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxPoolSize(maxPoolSize);
	}
	/**
	 * set max statements count
	 * @param maxStatements
	 */
	public void maxStatements(int maxStatements) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxStatements(maxStatements);
	}
	/**
	 * set min pool size
	 * @param minPoolSize
	 */
	public void minPoolSize(int minPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMinPoolSize(minPoolSize);
	}
	/**
	 * set jdbc password
	 * @param password jdbc password
	 */
	public void password(String password) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setPassword(password);
	}
	/**
	 * set jdbc user
	 * @param user jdbc user
	 */
	public void user(String user) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setUser(user);
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		super.init();
		ConsoleServer cs=Jazmin.server(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new C3P0DriverCommand());
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
				.print("user",user())
				.print("driverClass",driverClass())
				.print("initialPoolSize",initialPoolSize())
				.print("url",url())
				.print("loginTimeout",loginTimeout())
				.print("maxConnectionAge",maxConnectionAge())
				.print("maxIdleTime",maxIdleTime())
				.print("maxPoolSize",maxPoolSize())
				.print("minPoolSize",minPoolSize())
				.print("threadPoolSize",threadPoolSize())
				.print("statSql",isStatSql())
				.toString();
	}
}
