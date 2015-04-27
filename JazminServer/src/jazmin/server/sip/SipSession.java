/**
 * 
 */
package jazmin.server.sip;

import java.util.Date;

/**
 * @author yama
 *
 */
public class SipSession {
	SipServer server;
	//
	long sessionId;
	String callId;
	String remoteAddress;
	int remotePort;
	Object userObject;
	Date createTime;
	Date lastAccessTime;
	int sessionTimeout;
	//
	SipSession(SipServer server) {
		this.server=server;
		createTime=new Date();
		lastAccessTime=new Date();
	}
	
	/**
	 * @return the sessionId
	 */
	public long getSessionId() {
		return sessionId;
	}

	//
	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}
	/**
	 * @return the remoteAddress
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}
	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}
	/**
	 * @return the userObject
	 */
	public Object getUserObject() {
		return userObject;
	}
	
	/**
	 * @param userObject the userObject to set
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * @return the lastAccessTime
	 */
	public Date getLastAccessTime() {
		return lastAccessTime;
	}
	
	/**
	 * @return the sessionTimeout
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @param sessionTimeout the sessionTimeout to set
	 */
	public void setSessionTimeout(int sessionTimeout) {
		if(sessionTimeout<SipServer.MIN_SESSION_TIMEOUT){
			throw new IllegalArgumentException("session timeout must >="+SipServer.MIN_SESSION_TIMEOUT);
		}
		this.sessionTimeout = sessionTimeout;
	}

	//
	public void invalidate(){
		
	}
}
