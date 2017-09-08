/**
 * 
 */
package jazmin.server.msg;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.RateLimiter;
import jazmin.server.msg.codec.DefaultCodecFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class Session {
	private static Logger logger=LoggerFactory.get(Session.class);
	//
	String connectionType;
	int id;
	int messageType;
	String principal;
	String userAgent;
	Object userObject;
	NetworkChannel channel;
	long lastAccessTime;
	String remoteHostAddress;
	int remotePort;
	int requestId;
	long sentMessageCount;
	long receiveMessageCount;
	Set<String>channels;
	Date createTime;
	//
	RateLimiter rateLimiter;
	private AtomicBoolean processSyncServiceState;
	//
	Session(NetworkChannel channel) {
		setChannel(channel);
		lastAccess();
		sentMessageCount=0;
		receiveMessageCount=0;
		rateLimiter=new RateLimiter();
		processSyncServiceState=new AtomicBoolean();
		processSyncService(false);
		channels=Collections.synchronizedSet(new TreeSet<String>());
		createTime=new Date();
		connectionType="tcp";
	}
	//--------------------------------------------------------------------------
	//public interface
	
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * @return the connectionType
	 */
	public String getConnectionType() {
		return connectionType;
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
	 * @return the sentMessageCount
	 */
	public long getSentMessageCount() {
		return sentMessageCount;
	}

	/**
	 * @return the receiveMessageCount
	 */
	public long getReceiveMessageCount() {
		return receiveMessageCount;
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
	/**
	 * @return last access time
	 */
	public long getLastAccessTime(){
		return lastAccessTime;
	}
	/**
	 * push message to client
	 * @param serviceId the message id
	 * @param payload message payload 
	 */
	public void push(String serviceId,Object payload){
		if(serviceId==null){
			throw new IllegalArgumentException("serviceId can not be null.");
		}
		if(payload==null){
			throw new IllegalArgumentException("payload can not be null.");
		}
		ResponseMessage rsm=new ResponseMessage();
		rsm.requestId=0;
		rsm.serviceId=serviceId;
		rsm.responseObject=payload;
		sendMessage(rsm);
	}
	//
	/**
	 * push message to client
	 * @param serviceId the message id
	 * @param payload message payload
	 */
	public void pushRaw(String serviceId,byte[] payload) {
		if(serviceId==null){
			throw new IllegalArgumentException("serviceId can not be null.");
		}
		if(payload==null){
			throw new IllegalArgumentException("payload can not be null.");
		}
		ResponseMessage rsp=new ResponseMessage();
		rsp.requestId=0;
		rsp.serviceId=serviceId;
		rsp.rawData=payload;
		sendMessage(rsp);
	}
	/**
	 * send kick message and close session 
	 * @param message the message before session kicked
	 */
	public void kick(String message){
		if(logger.isDebugEnabled()){
			logger.debug("session kicked:{} {}",id,message);
		}
		if(channel!=null){
			channel.close();	
		}
	}
	
	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}
	/**
	 * @return all channel id of session joined 
	 */
	public List<String>getChannels(){
		return new ArrayList<>(channels);
	}
	//
	//--------------------------------------------------------------------------
	//
	//
	boolean isProcessSyncService(){
		return processSyncServiceState.get();
	}
	//
	void enterChannel(Channel c){
		channels.add(c.id);
	}
	//
	void  leaveChannel(Channel c){
		channels.remove(c.id);
	}
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
	void setChannel(NetworkChannel channel){
		this.channel=channel;
		remoteHostAddress=null;
		remotePort=0;
		if(channel!=null){
			SocketAddress remoteAddr=channel.getRemoteAddress();
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
	void receivedMessage(RequestMessage message){
		receiveMessageCount++;
		requestId=message.requestId;
		lastAccess();
	}
	
	/**
	 * @return the messageType
	 */
	public int getMessageType() {
		return messageType;
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
	void sendError(RequestMessage message,int code,String msg){
		ResponseMessage rsp=new ResponseMessage();
		rsp.requestId=(message==null)?0:message.requestId;
		rsp.statusCode=code;
		rsp.statusMessage=msg;
		rsp.serviceId="jazmin.error";
		sendMessage(rsp);
	}
	//
	void sendMessage(ResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
			sentMessageCount++;
			responseMessage.messageType=messageType;
			if(responseMessage.rawData!=null){
				responseMessage.messageType=DefaultCodecFactory.FORMAT_RAW;
			}
			if(responseMessage.responseObject==null){
				responseMessage.responseObject=ResponseMessage.emptyHashMap;
			}
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
		Session other = (Session) obj;
		if (id != other.id)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Session [id=" + id + ", principal=" + principal + ", channel="
				+ channel + "]";
	}

}
