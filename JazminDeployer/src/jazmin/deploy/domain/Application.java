/**
 * 
 */
package jazmin.deploy.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author yama
 *
 */
public class Application{
	
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
	
	static Map<String,String>typeLayerMap=new HashMap<String, String>();
	
	//
	public static final String TYPE_JAZMIN_RPC="jazmin-rpc";
	public static final String TYPE_JAZMIN_WEB="jazmin-web";
	public static final String TYPE_JAZMIN_MSG="jazmin-msg";
	public static final String TYPE_JAZMIN_FTP="jazmin-ftp";
	public static final String TYPE_JAZMIN_RTMP="jazmin-rtmp";
	public static final String TYPE_JAZMIN_SIP="jazmin-sip";
	public static final String TYPE_JAZMIN_RELAY="jazmin-relay";
	public static final String TYPE_JAZMIN_RTSP="jazmin-rtsp";
	public static final String TYPE_JAZMIN_CDN="jazmin-cdn";
	public static final String TYPE_JAZMIN_FILE="jazmin-file";
	
	
	//
	public static final String TYPE_MYSQL="mysql";
	public static final String TYPE_MEMCACHED="memcached";
	public static final String TYPE_HAPROXY="haproxy";
	public static final String TYPE_REDIS="redis";
	public static final String TYPE_INFLUXDB="influxdb";
	//
	public static final String TYPE_BROSWER="broswer";
	public static final String TYPE_CLIENT="client";
	public static final String TYPE_API="api";
	//
	public static final String LAYER_USER="User Tier";
	public static final String LAYER_PROXY="Proxy Tier";
	public static final String LAYER_WEB="Web Tier";
	public static final String LAYER_APP="App Tier";
	public static final String LAYER_CACHE="Cache Tier";
	public static final String LAYER_DB="Storage Tier";
	public static final String LAYER_OTHER="Other Tier";
	//
	static{
		typeLayerMap.put(TYPE_JAZMIN_RPC,LAYER_APP);
		//
		typeLayerMap.put(TYPE_JAZMIN_WEB,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_MSG,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_FTP,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_RTMP,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_SIP,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_RELAY,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_RTSP,LAYER_WEB);
		typeLayerMap.put(TYPE_JAZMIN_CDN,LAYER_WEB);
		//
		typeLayerMap.put(TYPE_MYSQL,LAYER_DB);
		typeLayerMap.put(TYPE_JAZMIN_FILE,LAYER_DB);
		typeLayerMap.put(TYPE_REDIS,LAYER_DB);
		typeLayerMap.put(TYPE_INFLUXDB,LAYER_DB);
		
		
		
		typeLayerMap.put(TYPE_MEMCACHED,LAYER_CACHE);
		typeLayerMap.put(TYPE_HAPROXY,LAYER_PROXY);
		//
		typeLayerMap.put(TYPE_BROSWER,LAYER_USER);
		typeLayerMap.put(TYPE_CLIENT,LAYER_USER);
		//
		typeLayerMap.put(TYPE_API,LAYER_OTHER);
	}
	//
	public String id;
	public String system;
	public List<String>depends;
	public String type;
	public int priority;
	public String scmUser;
	public String scmPassword;
	public String scmPath;
	//
	public String antTarget;
	
	public Map<String,String>properties;
	//
	public Application() {
		type="";
		depends=new LinkedList<String>();
		properties=new HashMap<String, String>();
	}
	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}
	/**
	 * @param system the system to set
	 */
	public void setSystem(String system) {
		this.system = system;
	}
	/**
	 * @return the depends
	 */
	public List<String> getDepends() {
		return depends;
	}
	/**
	 * @param depends the depends to set
	 */
	public void setDepends(List<String> depends) {
		this.depends = depends;
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
	//
	public String getLayer(){
		String t=typeLayerMap.get(this.type);
		if(t==null){
			t=LAYER_OTHER;
		}
		return t;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Application other = (Application) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
