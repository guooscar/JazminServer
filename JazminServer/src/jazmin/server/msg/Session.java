/**
 * 
 */
package jazmin.server.msg;


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
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class Session {
	private static Logger logger=LoggerFactory.get(Session.class);
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
	int totalMessageCount;
	boolean isActive;
	Set<String>channels;
	Date createTime;
	//
	private int frequencyCounter;
	private long frequencyTime;
	private int maxFrequencyCountPerSec;
	private AtomicBoolean processSyncServiceState;
	//
	Session(io.netty.channel.Channel channel) {
		setChannel(channel);
		lastAccess();
		totalMessageCount=0;
		maxFrequencyCountPerSec=10;
		resetFrequencyState();
		processSyncServiceState=new AtomicBoolean();
		processSyncService(false);
		channels=new TreeSet<String>();
		createTime=new Date();
	}
	//--------------------------------------------------------------------------
	//public interface
	/**
	 * @return the createTime
	 */
	public Date createTime() {
		return createTime;
	}
	/**
	 * @return the id
	 */
	public int id() {
		return id;
	}
	/**
	 * @return the totalMessageCount
	 */
	public int totalMessageCount() {
		return totalMessageCount;
	}
	/**
	 * @return the principal
	 */
	public String principal() {
		return principal;
	}
	/**
	 * @return the userObject
	 */
	public Object userObject() {
		return userObject;
	}
	/**
	 * @param userObject the userObject to set
	 */
	public void userObject(Object userObject) {
		this.userObject = userObject;
	}
	/**
	 * @return the remoteHostAddress
	 */
	public String remoteHostAddress() {
		return remoteHostAddress;
	}
	/**
	 * @return the remotePort
	 */
	public int remotePort() {
		return remotePort;
	}

	//
	public long lastAccessTime(){
		return lastAccessTime;
	}
	//
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
		rsm.responseMessages.put("payload",payload);
		sendMessage(rsm);
	}
	//
	//
	public void push(byte[] bb) {
		ResponseMessage rsp=new ResponseMessage();
		rsp.rawData=bb;
		sendMessage(rsp);
	}
	//
	public void kick(String message){
		if(logger.isDebugEnabled()){
			logger.debug("session kicked:{}",message);
		}
		sendError(null,ResponseMessage.SC_KICKED, message);
		if(channel!=null){
			channel.close();	
		}
	}
	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}
	/**
	 * @return the userAgent
	 */
	public String userAgent() {
		return userAgent;
	}
	/**
	 * 
	 */
	public void enterChannel(Channel c){
		channels.add(c.id);
	}
	/**
	 * 
	 */
	public void  leaveChannel(Channel c){
		channels.remove(c.id);
	}
	/**
	 * 
	 */
	public List<String>channels(){
		return new ArrayList<>(channels);
	}
	//
	public boolean processSyncService(){
		return processSyncServiceState.get();
	}
	//
	//--------------------------------------------------------------------------
	/**
	 * @param isActive the isActive to set
	 */
	void setActive(boolean isActive) {
		this.isActive = isActive;
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
	//
	boolean isProcessSyncService(){
		return processSyncServiceState.get();
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
		int maxRequestCount=maxFrequencyCountPerSec*10;
		if(maxRequestCount<=0){
			return false;
		}
		frequencyCounter++;
		final int TEN_SECONDS=10*1000;
		//sample every 10 second
		long now=System.currentTimeMillis();
		if((now-frequencyTime)>TEN_SECONDS){
			resetFrequencyState();
		}else{
			if(frequencyCounter>maxRequestCount){
				return true;
			}
		}
		return false;
	}
	//
	void resetFrequencyState(){
		frequencyTime=System.currentTimeMillis();
		frequencyCounter=0;
	}
	//
	void receivedMessage(RequestMessage message){
		totalMessageCount++;
		requestId=message.requestId;
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
	void sendError(RequestMessage message,int code,String msg){
		ResponseMessage rsp=new ResponseMessage();
		rsp.requestId=(message==null)?0:message.requestId;
		rsp.statusCode=code;
		rsp.statusMessage=msg;
		sendMessage(rsp);
	}
	//
	void sendMessage(ResponseMessage responseMessage){
		lastAccess();
		if(channel!=null){
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

}
