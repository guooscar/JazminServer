/**
 * 
 */
package jazmin.deploy.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class Machine{
	public String id;
	public boolean isAlive;
	public String privateHost;
	public String publicHost;
	public String sshUser;
	public String sshPassword;
	public String rootSshPassword;
	public int sshTimeout;
	public int sshPort;
	//
	public int vncPort;
	public String vncPassword;
	//
	public String jazminHome;
	public String memcachedHome;
	public String haproxyHome;
	public Map<String,String>properties;
	//
	public Machine() {
		super();
		properties=new HashMap<String, String>();
	}
	//
	public int getSshTimeout(){
		if(sshTimeout<=0){
			return 5000;
		}
		return sshTimeout;
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
	public int getVncPort() {
		return vncPort;
	}
	public void setVncPort(int vncPort) {
		this.vncPort = vncPort;
	}
	public String getVncPassword() {
		return vncPassword;
	}
	public void setVncPassword(String vncPassword) {
		this.vncPassword = vncPassword;
	}
	
}
