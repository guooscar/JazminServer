/**
 * 
 */
package jazmin.driver.rpc;

/**
 * @author yama
 * 26 Dec, 2014
 */
public interface PushCallback {
	void callback(String cluster,String serviceId,Object payload)throws Exception;
}
