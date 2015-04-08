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
	//
	
	/**
	 */
	public int checkoutTimeout() {
		return dataSource.getCheckoutTimeout();
	}
	/**
	 */
	public String driverClass() {
		return dataSource.getDriverClass();
	}
	/**
	 */
	public int initialPoolSize() {
		return dataSource.getInitialPoolSize();
	}
	/**
	 */
	public String url() {
		return dataSource.getJdbcUrl();
	}
	/**
	 */
	public int loginTimeout()  {
		try {
			return dataSource.getLoginTimeout();
		} catch (SQLException e) {
			return 0;
		}
	}
	/**
	 */
	public int maxConnectionAge() {
		return dataSource.getMaxConnectionAge();
	}
	/**
	 */
	public int maxIdleTime() {
		return dataSource.getMaxIdleTime();
	}
	/**
	 */
	public int maxPoolSize() {
		return dataSource.getMaxPoolSize();
	}
	/**
	 */
	public int minPoolSize() {
		return dataSource.getMinPoolSize();
	}
	/**
	 */
	public int numConnections() throws SQLException {
		return dataSource.getNumConnections();
	}
	/**
	 */
	public int numIdleConnections() throws SQLException {
		return dataSource.getNumIdleConnections();
	}
	/**
	 */
	public int threadPoolNumActiveThreads() throws SQLException {
		return dataSource.getThreadPoolNumActiveThreads();
	}
	/**
	 */
	public int threadPoolNumIdleThreads() throws SQLException {
		return dataSource.getThreadPoolNumIdleThreads();
	}
	/**
	 */
	public int threadPoolNumTasksPending() throws SQLException {
		return dataSource.getThreadPoolNumTasksPending();
	}
	/**
	 */
	public int threadPoolSize() {
		try {
			return dataSource.getThreadPoolSize();
		} catch (SQLException e) {
			return 0;
		}
	}
	/**
	 */
	public String user() {
		return dataSource.getUser();
	}
	/**
	 */
	public boolean autoCommitOnClose() {
		return dataSource.isAutoCommitOnClose();
	}
	/**
	 */
	public void checkoutTimeout(int checkoutTimeout) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setCheckoutTimeout(checkoutTimeout);
	}
	/**
	 */
	public void driverClass(String driverClass) throws PropertyVetoException {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setDriverClass(driverClass);
	}
	/**
	 */
	public void initialPoolSize(int initialPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setInitialPoolSize(initialPoolSize);
	}
	/**
	 */
	public void url(String jdbcUrl) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setJdbcUrl(jdbcUrl);
	}
	/**
	 */
	public void loginTimeout(int seconds) throws SQLException {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setLoginTimeout(seconds);
	}
	/**
	 */
	public void maxConnectionAge(int maxConnectionAge) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxConnectionAge(maxConnectionAge);
	}
	/**
	 */
	public void maxIdleTime(int maxIdleTime) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxIdleTime(maxIdleTime);
	}
	/**
	 */
	public void maxPoolSize(int maxPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxPoolSize(maxPoolSize);
	}
	/**
	 */
	public void maxStatements(int maxStatements) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMaxStatements(maxStatements);
	}
	/**
	 */
	public void minPoolSize(int minPoolSize) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setMinPoolSize(minPoolSize);
	}
	/**
	 */
	public void password(String password) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		dataSource.setPassword(password);
	}
	/**
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
