/**
 * 
 */
package jazmin.deploy.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yama
 *
 */
public class Application extends BaseDomain{
	public static final String TYPE_JAZMIN_RPC="jazmin-rpc";
	public static final String TYPE_JAZMIN_WEB="jazmin-web";
	public static final String TYPE_JAZMIN_MSG="jazmin-msg";
	public static final String TYPE_JAZMIN_FTP="jazmin-ftp";
	public static final String TYPE_JAZMIN_RTMP="jazmin-rtmp";
	public static final String TYPE_JAZMIN_SIP="jazmin-sip";
	
	public static final String TYPE_MYSQL="mysql";
	public static final String TYPE_MEMCACHED="memcached";
	public static final String TYPE_HAPROXY="haproxy";
	public static final String TYPE_REDIS="redis";
	//
	public static final String LAYER_USER="user";
	public static final String LAYER_PROXY="proxy";
	public static final String LAYER_WEB="web";
	public static final String LAYER_APP="app";
	public static final String LAYER_CACHE="cache";
	public static final String LAYER_DB="db";
	public static final String LAYER_OTHER="other";
	//
	public String system;
	public List<String>depends;
	public String layer;
	public String type;
	//
	public Application() {
		super();	
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
	 * @return the layer
	 */
	public String getLayer() {
		return layer;
	}
	/**
	 * @param layer the layer to set
	 */
	public void setLayer(String layer) {
		this.layer = layer;
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
	
}
