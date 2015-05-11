package jazmin.deploy.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author yama
 *
 */
public class BaseDomain {
	public String id;
	public Map<String,String>properties;
	public BaseDomain() {
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
	
}
