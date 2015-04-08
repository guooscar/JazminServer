
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
public class JDBCConnectionDriver extends ConnectionDriver{
	private String user;
	private String password;
	private String url;
	private String driver;
	//
	public JDBCConnectionDriver() {
	}
	/**
	
	 * @return the user */
	public String user() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void user(String user) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.user = user;
	}

	/**
	
	 * @return the driver */
	public String driver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void driver(String driver) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.driver = driver;
	}
	/**
	
	 * @return the password */
	public String password() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void password(String password) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.password = password;
	}

	/**
	
	 * @return the url */
	public String url() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void url(String url) {
		if(inited()){
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
				.print("Url",url())
				.print("User",user())
				.print("Driver",driver())
				.toString();
	}
	//
}
