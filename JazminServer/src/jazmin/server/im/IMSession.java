/**
 * 
 */
package jazmin.server.im;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.RateLimiter;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class IMSession {
	private static Logger logger=LoggerFactory.get(IMSession.class);
	//
	int id;
	String principal;
	String userAgent;
	Object userObject;
	io.netty.channel.Channel channel;
	long lastAccessTime;
	String remoteHostAddress;
	int remotePort;
	int requestId;
	long receiveMessageCount;
	long sentMessageCount;
	Set<String>channels;
	Date createTime;
	//
	RateLimiter rateLimiter;
	private AtomicBoolean processSyncServiceState;
	//
	IMSession(io.netty.channel.Channel channel) {
		setChannel(channel);
		lastAccess();
		receiveMessageCount=0;
		sentMessageCount=0;
		rateLimiter=new RateLimiter();
		processSyncServiceState=new AtomicBoolean();
		processSyncService(false);
		channels=new TreeSet<String>();
		createTime=new Date();
	}
	//--------------------------------------------------------------------------
	//
	
	//
	//public interface
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}


	/**
	 * @return the maxRequestCountPerSecond
	 */
	public int getMaxRequestCountPerSecond() {
		return rateLimiter.getMaxRequestCountPerSecond();
	}

	/**
	 * @param maxRequestCountPerSecond the maxRequestCountPerSecond to set
	 */
	public void setMaxRequestCountPerSecond(int maxRequestCountPerSecond) {
		this.rateLimiter.setMaxRequestCountPerSecond(maxRequestCountPerSecond);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the receiveMessageCount
	 */
	public long getReceiveMessageCount() {
		return receiveMessageCount;
	}

	/**
	 * @return the sentMessageCount
	 */
	public long getSentMessageCount() {
		return sentMessageCount;
	}

	/**
	 * @return the principal
	 */
	public String getPrincipal() {
		return principal;
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
	 * @return the remoteHostAddress
	 */
	public String getRemoteHostAddress() {
		return remoteHostAddress;
	}
	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}

	//
	public long getLastAccessTime(){
		return lastAccessTime;
	}
	//
	public void push(byte[] bb) {
		IMResponseMessage rsp=new IMResponseMessage();
		rsp.rawData=bb;
		sendMessage(rsp);
	}
	//
	public void kick(String msg){
		sendError(null,IMResponseMessage.SC_KICKED,msg);
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			logger.catching(e);
		}	
	}
	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}
	/**
	 * 
	 */
	void enterChannel(IMChannel c){
		channels.add(c.id);
	}
	/**
	 * 
	 */
	void  leaveChannel(IMChannel c){
		channels.remove(c.id);
	}
	/**
	 * 
	 */
	public List<String>getChannels(){
		return new ArrayList<>(channels);
	}
	//
	public boolean isProcessSyncService(){
		return processSyncServiceState.get();
	}
	//
	//--------------------------------------------------------------------------
	//
	void lastAccess(){
		lastAccessTime=System.currentTimeMillis();
	}
	/**
	 * @param userAgent the userAgent to set
	 */
	void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @param principal the principal to set
	 */
	void setPrincipal(String principal) {
		this.principal = principal;
	}
	
	/*
	 */
	void setId(int id) {
		this.id = id;
	}
	//
	void processSyncService(boolean isProcess){
		processSyncServiceState.set(isProcess);
	}
	//
	/**/
	void setChannel(io.netty.channel.Channel channel){
		this.channel=channel;
		remoteHostAddress=null;
		remotePort=0;
		if(channel!=null){
			SocketAddress remoteAddr=channel.remoteAddress();
			if(remoteAddr!=null){
				InetSocketAddress addr=(InetSocketAddress) remoteAddr;
				InetAddress ad=addr.getAddress();
				remoteHostAddress=ad.getHostAddress();
				remotePort=addr.getPort();
			}		
		}
	}
	//
	boolean isFrequencyReach(){
		return rateLimiter.accessAndTest();
	}
	//
	void receivedMessage(IMRequestMessage message){
		receiveMessageCount++;
		//requestId=message.requestId;
		lastAccess();
	}
	/*
	 * @return the requestId
	 */
	int getRequestId() {
		return requestId;
	}
	/*
	 * @param requestId the requestId to set
	 */
	void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	//
	void sendError(IMRequestMessage message,int code,String msg){
		//msg not use
		IMResponseMessage rsp=new IMResponseMessage();
		//rsp.requestId=(message==null)?0:message.requestId;
		rsp.statusCode=code;
		sendMessage(rsp);
	}
	//
	void sendMessage(IMResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
			sentMessageCount++;
			channel.writeAndFlush(responseMessage);
		}
	}
	//
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	//
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IMSession other = (IMSession) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
