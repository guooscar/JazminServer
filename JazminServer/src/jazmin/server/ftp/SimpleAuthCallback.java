/**
 * 
 */
package jazmin.server.ftp;


/**
 * @author g2131
 *
 */
public interface SimpleAuthCallback {
	
	public FTPUserInfo authenticate(String user,String password)throws Exception;
	public FTPUserInfo getUserByName(String user)throws Exception;
}
