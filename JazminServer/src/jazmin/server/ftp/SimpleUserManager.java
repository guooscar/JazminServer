/**
 * 
 */
package jazmin.server.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 * @author yama
 *
 */
public class SimpleUserManager implements FTPUserManager{
	private SimpleAuthCallback callback;
	public SimpleUserManager(SimpleAuthCallback callback) {
		this.callback=callback;
	}
	@Override
	public void delete(String arg0) throws FtpException {
		throw new IllegalArgumentException("not support");
	}

	//
	@Override
	public String[] getAllUserNames() throws FtpException {
		throw new IllegalArgumentException("not support");
	}
	//
	@Override
	public void save(User arg0) throws FtpException {
		throw new IllegalArgumentException("not support");
	}
	//
	@Override
	public String getAdminName() throws FtpException {
		throw new IllegalArgumentException("not support");
		
	}
	//
	@Override
	public boolean isAdmin(String user) throws FtpException {
		throw new IllegalArgumentException("not support");
		
	}
	//--------------------------------------------------------------------------
	@Override
	public User authenticate(Authentication auth)
			throws AuthenticationFailedException {
		if(auth instanceof UsernamePasswordAuthentication){
			try {
				UsernamePasswordAuthentication ua=(UsernamePasswordAuthentication) auth;
				FTPUserInfo user= callback.authenticate(ua.getUsername(),ua.getPassword());
				//
				if(user==null){
					throw new AuthenticationFailedException("Authentication failed");
				}
				return getUser(user);
			} catch (Exception e) {
				throw new AuthenticationFailedException(e);
			}	
		}else{
			 throw new AuthenticationFailedException("Authentication failed");
		}
		
	}
	//
	private User getUser(FTPUserInfo ftpUser){
		BaseUser user = new BaseUser();
		user.setName(ftpUser.userName);
		user.setEnabled(ftpUser.enableFlag);
		user.setHomeDirectory(ftpUser.homeDirectory);
		List<Authority> authorities = new ArrayList<Authority>();
		if (ftpUser.writePermission) {
			authorities.add(new WritePermission());
		}
		authorities.add(new ConcurrentLoginPermission(
				ftpUser.maxLoginNumber, ftpUser.maxLoginperip));
		authorities.add(new TransferRatePermission(ftpUser.downloadRate,
				ftpUser.uploadRate));
		user.setAuthorities(authorities);
		user.setMaxIdleTime(ftpUser.idleTime);
		return user;
	}
	//
	@Override
	public boolean doesExist(String user) throws FtpException {
		return true;
	}
	//
	@Override
	public User getUserByName(String u) throws FtpException {
		try {
			FTPUserInfo ftpUser = callback.getUserByName(u);
			return getUser(ftpUser);
		} catch (Exception e) {
			throw new FtpException(e);
		}
	}
}
