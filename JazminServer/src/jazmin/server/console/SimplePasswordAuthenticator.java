/**
 * 
 */
package jazmin.server.console;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * @author yama
 *
 */
public class SimplePasswordAuthenticator implements Authenticator{
	private Map<String,String>users;
	public SimplePasswordAuthenticator() {
		users=new ConcurrentHashMap<String, String>();
	}
	//
	public void setUser(String u,String p){
		if(u==null||p==null){
			throw new IllegalArgumentException("user or password can not be null.");
		}
		users.put(u, p);
	}
	//
	public void removeUser(String u){
		users.remove(u);
	}
	//
	@Override
	public boolean auth(String user, String password) {
		if(user==null||password==null){
			return false;
		}
		String p=users.get(user);
		return password.equals(p);
	}

}
