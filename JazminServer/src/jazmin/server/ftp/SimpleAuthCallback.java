/**
 * 
 */
package jazmin.server.ftp;


/**
 * @author g2131
 *
 */
public interface SimpleAuthCallback {
	
	public FtpUserInfo authenticate(String user,String password)throws Exception;
	public FtpUserInfo getUserByName(String user)throws Exception;
}
