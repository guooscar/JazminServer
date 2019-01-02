/**
 * 
 */
package jazmin.server.mysqlproxy;

/**
 * @author yama
 *
 */
public interface ProxyRuleAuthProvider {
	Database auth(ProxySession session);	
}
