
package jazmin.driver.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jazmin.misc.InfoBuilder;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class JdbcConnectionDriver extends ConnectionDriver{
	private String user;
	private String password;
	private String url;
	private String driver;
	//
	public JdbcConnectionDriver() {
	}
	/**
	
	 * @return the user */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.user = user;
	}

	/**
	
	 * @return the driver */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(String driver) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.driver = driver;
	}
	/**
	
	 * @return the password */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.password = password;
	}

	/**
	
	 * @return the url */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.url = url;
	}
	//
	@Override
	public Connection getWorkConnection() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			throw new ConnectionException(e);
		}
	}
	//--------------------------------------------------------------------------
	@Override
	public void init()throws Exception {
		super.init();
		if(driver==null){
			throw new IllegalArgumentException("driver can not be null.");
		}
		Class.forName(driver);	
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
				.print("Url",getUrl())
				.print("User",getUser())
				.print("Driver",getDriver())
				.toString();
	}
	//
}
