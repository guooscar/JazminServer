/**
 * 
 */
package jazmin.deploy.domain;

/**
 * @author yama
 * 30 Dec, 2014
 */
public class User {
	public String id;
	public String password;
	/**
	 * @return the user
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param user the user to set
	 */
	public void setId(String user) {
		this.id = user;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
