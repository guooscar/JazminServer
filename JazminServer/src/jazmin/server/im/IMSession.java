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

/**
 * @author yama
 * 25 Dec, 2014
 */
public class IMSession {
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
	IMSession(io.netty.channel.Channel channel) {
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
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the totalMessageCount
	 */
	public int getTotalMessageCount() {
		return totalMessageCount;
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
	void receivedMessage(IMRequestMessage message){
		totalMessageCount++;
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
