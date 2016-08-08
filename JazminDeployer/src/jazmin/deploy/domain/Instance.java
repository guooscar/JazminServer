/**
 * 
 */
package jazmin.deploy.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class Instance{
	//
	public static final String P_JAZMIN_LOG_LEVEL="jazmin.log.level";
	public static final String P_JAZMIN_LOG_FILE="jazmin.log.file";
	//
	public static final String P_JAZMIN_CONSOLE_USER="jazmin.console.user";
	public static final String P_JAZMIN_CONSOLE_PWD="jazmin.console.password";
	//
	public static final String P_HAPROXY_DOMAIN="haproxy.domain";
	public static final String P_HAPROXY_STATURL="haproxy.staturl";
	//
	public static final String P_MEMCACHED_SIZE="memcached.size";
	//
	public static final String P_NO_CHECK_IP="nocheckip";
	public static final String P_DB_PUBLIC_PORT="db.public.port";
	public static final String P_DB_PUBLIC_USER="db.public.user";
	public static final String P_DB_PUBLIC_PASSWORD="db.public.password";
	//
	public String id;
	public String cluster;
	public String appId;
	public Application application;
	public String machineId;
	public Machine machine;
	public int port;
	public String user;
	public String password;
	public boolean isAlive;
	public int priority;
	public String packageVersion;
	public Map<String,String>properties;
	public Date lastAliveTime;
	//
	public Instance() {
		super();
		properties=new HashMap<String, String>();
	}
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
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
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
	 * @return the app
	 */
	public String getApp() {
		return appId;
	}
	/**
	 * @param app the app to set
	 */
	public void setApp(String app) {
		this.appId = app;
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
	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the application
	 */
	public Application getApplication() {
		return application;
	}
	/**
	 * @param application the application to set
	 */
	public void setApplication(Application application) {
		this.application = application;
	}
	/**
	 * @return the lastAliveTime
	 */
	public Date getLastAliveTime() {
		return lastAliveTime;
	}
	/**
	 * @param lastAliveTime the lastAliveTime to set
	 */
	public void setLastAliveTime(Date lastAliveTime) {
		this.lastAliveTime = lastAliveTime;
	}
	
	
}
