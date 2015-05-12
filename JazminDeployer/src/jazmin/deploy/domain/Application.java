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
public class Application extends BaseDomain{
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
	
	//
	public static final String TYPE_MYSQL="mysql";
	public static final String TYPE_MEMCACHED="memcached";
	public static final String TYPE_HAPROXY="haproxy";
	public static final String TYPE_REDIS="redis";
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
	public static final String LAYER_DB="DB Tier";
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
		//
		typeLayerMap.put(TYPE_MYSQL,LAYER_DB);
		typeLayerMap.put(TYPE_MEMCACHED,LAYER_CACHE);
		typeLayerMap.put(TYPE_HAPROXY,LAYER_PROXY);
		typeLayerMap.put(TYPE_REDIS,LAYER_CACHE);
		//
		typeLayerMap.put(TYPE_BROSWER,LAYER_USER);
		typeLayerMap.put(TYPE_CLIENT,LAYER_USER);
		//
		typeLayerMap.put(TYPE_API,LAYER_OTHER);
	}
	//
	public String system;
	public List<String>depends;
	public String type;
	public int priority;
	//
	public Application() {
		super();	
		type="";
		depends=new LinkedList<String>();
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
	
}
