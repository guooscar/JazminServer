/**
 * 
 */
package jazmin.deploy.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yama
 * 30 Dec, 2014
 */
public class User {
	public static String ADMIN="admin";
	//
	public String id;
	public String password;
	public List<String>machines;
	public List<String>applicationSystems;
	public List<String>instanceClusters;
	public User() {
		machines=new ArrayList<String>();
		applicationSystems=new ArrayList<String>();
		instanceClusters=new ArrayList<String>();
	}
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
	/**
	 * @return the machines
	 */
	public List<String> getMachines() {
		return machines;
	}
	/**
	 * @param machines the machines to set
	 */
	public void setMachines(List<String> machines) {
		this.machines = machines;
	}
	/**
	 * @return the applicationSystems
	 */
	public List<String> getApplicationSystems() {
		return applicationSystems;
	}
	/**
	 * @param applicationSystems the applicationSystems to set
	 */
	public void setApplicationSystems(List<String> applicationSystems) {
		this.applicationSystems = applicationSystems;
	}
	/**
	 * @return the instanceClusters
	 */
	public List<String> getInstanceClusters() {
		return instanceClusters;
	}
	/**
	 * @param instanceClusters the instanceClusters to set
	 */
	public void setInstanceClusters(List<String> instanceClusters) {
		this.instanceClusters = instanceClusters;
	}

}
