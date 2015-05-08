/**
 * 
 */
package jazmin.server.ftp;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import org.apache.ftpserver.ftplet.User;


/**
 * @author yama
 * 26 Mar, 2015
 */
public class FtpSession {
	org.apache.ftpserver.ftplet.FtpSession session;

	/**
	 * @param arg0
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return session.getAttribute(arg0);
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getClientAddress()
	 */
	public InetSocketAddress getClientAddress() {
		return session.getClientAddress();
	}


	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getConnectionTime()
	 */
	public Date getConnectionTime() {
		return session.getConnectionTime();
	}



	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getFailedLogins()
	 */
	public int getFailedLogins() {
		return session.getFailedLogins();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getFileOffset()
	 */
	public long getFileOffset() {
		return session.getFileOffset();
	}


	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getLanguage()
	 */
	public String getLanguage() {
		return session.getLanguage();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getLastAccessTime()
	 */
	public Date getLastAccessTime() {
		return session.getLastAccessTime();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getLoginTime()
	 */
	public Date getLoginTime() {
		return session.getLoginTime();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getMaxIdleTime()
	 */
	public int getMaxIdleTime() {
		return session.getMaxIdleTime();
	}


	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getServerAddress()
	 */
	public InetSocketAddress getServerAddress() {
		return session.getServerAddress();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getSessionId()
	 */
	public UUID getSessionId() {
		return session.getSessionId();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getUser()
	 */
	public FtpUserInfo getUser() {
		FtpUserInfo info=new FtpUserInfo();
		User user=session.getUser();
		if(user==null){
			return null;
		}
		info.userName=user.getName();
		info.homeDirectory=user.getHomeDirectory();
		info.idleTime=user.getMaxIdleTime();
		return info;
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#getUserArgument()
	 */
	public String getUserArgument() {
		return session.getUserArgument();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#isLoggedIn()
	 */
	public boolean isLoggedIn() {
		return session.isLoggedIn();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpSession#isSecure()
	 */
	public boolean isSecure() {
		return session.isSecure();
	}

	/**
	 * @param arg0
	 * @see org.apache.ftpserver.ftplet.FtpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		session.removeAttribute(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.ftpserver.ftplet.FtpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		session.setAttribute(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.ftpserver.ftplet.FtpSession#setMaxIdleTime(int)
	 */
	public void setMaxIdleTime(int arg0) {
		session.setMaxIdleTime(arg0);
	}

}
