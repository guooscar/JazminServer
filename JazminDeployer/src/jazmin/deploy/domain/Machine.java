/**
 * 
 */
package jazmin.deploy.domain;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class Machine {
	public String id;
	public boolean isAlive;
	public String privateHost;
	public String publicHost;
	public String sshUser;
	public String sshPassword;
	public int sshPort;
	public String jazminHome;
	public String memcachedHome;
	public String haproxyHome;
	//
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the isAlive
	 */
	public boolean isAlive() {
		return isAlive;
	}
	/**
	 * @param isAlive the isAlive to set
	 */
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	/**
	 * @return the privateHost
	 */
	public String getPrivateHost() {
		return privateHost;
	}
	/**
	 * @param privateHost the privateHost to set
	 */
	public void setPrivateHost(String privateHost) {
		this.privateHost = privateHost;
	}
	/**
	 * @return the publicHost
	 */
	public String getPublicHost() {
		return publicHost;
	}
	/**
	 * @param publicHost the publicHost to set
	 */
	public void setPublicHost(String publicHost) {
		this.publicHost = publicHost;
	}
	/**
	 * @return the sshUser
	 */
	public String getSshUser() {
		return sshUser;
	}
	/**
	 * @param sshUser the sshUser to set
	 */
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	/**
	 * @return the sshPassword
	 */
	public String getSshPassword() {
		return sshPassword;
	}
	/**
	 * @param sshPassword the sshPassword to set
	 */
	public void setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
	}
	/**
	 * @return the sshPort
	 */
	public int getSshPort() {
		return sshPort;
	}
	/**
	 * @param sshPort the sshPort to set
	 */
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	/**
	 * @return the jazminHome
	 */
	public String getJazminHome() {
		return jazminHome;
	}
	/**
	 * @param jazminHome the jazminHome to set
	 */
	public void setJazminHome(String jazminHome) {
		this.jazminHome = jazminHome;
	}
	/**
	 * @return the memcachedHome
	 */
	public String getMemcachedHome() {
		return memcachedHome;
	}
	/**
	 * @param memcachedHome the memcachedHome to set
	 */
	public void setMemcachedHome(String memcachedHome) {
		this.memcachedHome = memcachedHome;
	}
	/**
	 * @return the haproxyHome
	 */
	public String getHaproxyHome() {
		return haproxyHome;
	}
	/**
	 * @param haproxyHome the haproxyHome to set
	 */
	public void setHaproxyHome(String haproxyHome) {
		this.haproxyHome = haproxyHome;
	}
	
}
