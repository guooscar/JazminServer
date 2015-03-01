/**
 * 
 */
package jazmin.server.console;

/**
 * @author yama
 * 7 Jan, 2015
 */
@FunctionalInterface
public interface Authenticator {
	boolean auth(String user,String password);
}
