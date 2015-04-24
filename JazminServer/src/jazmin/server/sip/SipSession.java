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
	//
	public void invalidate(){
		
	}
}
