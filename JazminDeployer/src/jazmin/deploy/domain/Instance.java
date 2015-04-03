/**
 * 
 */
package jazmin.deploy.domain;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class Instance {
	public static final String TYPE_JAZMIN_APP="jazmin-rpc";
	public static final String TYPE_JAZMIN_WEB="jazmin-web";
	public static final String TYPE_JAZMIN_MSG="jazmin-msg";
	public static final String TYPE_MYSQL="mysql";
	public static final String TYPE_MEMCACHED="memcached";
	public static final String TYPE_HAPROXY="haproxy";
	//
	public String id;
	public String cluster;
	public String type;
	public String app;
	public String machineId;
	public Machine machine;
	public int port;
	public String user;
	public String password;
	public boolean isAlive;
	public int priority;
	public String packageVersion;
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
	 * @return the cluster
	 */
	public String getCluster() {
		return cluster;
	}
	/**
	 * @param cluster the cluster to set
	 */
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the app
	 */
	public String getApp() {
		return app;
	}
	/**
	 * @param app the app to set
	 */
	public void setApp(String app) {
		this.app = app;
	}
	/**
	 * @return the machineId
	 */
	public String getMachineId() {
		return machineId;
	}
	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	/**
	 * @return the machine
	 */
	public Machine getMachine() {
		return machine;
	}
	/**
	 * @param machine the machine to set
	 */
	public void setMachine(Machine machine) {
		this.machine = machine;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	/**
	 * @return the packageVersion
	 */
	public String getPackageVersion() {
		return packageVersion;
	}
	/**
	 * @param packageVersion the packageVersion to set
	 */
	public void setPackageVersion(String packageVersion) {
		this.packageVersion = packageVersion;
	}
	
}
