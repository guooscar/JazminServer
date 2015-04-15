/**						JAZMIN SERVER SOURCE FILE
--------------------------------------------------------------------------------
	     	  ___  _______  _______  __   __  ___   __    _ 		
		     |   ||   _   ||       ||  |_|  ||   | |  |  | |		
		     |   ||  |_|  ||____   ||       ||   | |   |_| |		
		     |   ||       | ____|  ||       ||   | |       |		
		  ___|   ||       || ______||       ||   | |  _    |		
		 |       ||   _   || |_____ | ||_|| ||   | | | |   |		
		 |__yama_||__| |__||_______||_|   |_||___| |_|  |__|	 
		 
--------------------------------------------------------------------------------
********************************************************************************
 							Copyright (c) 2015 yama.
 This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 ANY use of this software MUST be subject to the consent of yama.

********************************************************************************
*/
package jazmin.server.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class Channel {
	String id;
	Map<String,Session>sessions;
	long createTime;
	Object userObject;
	MessageServer messageServer;
	boolean autoRemoveDisconnectedSession;
	//
	Channel(MessageServer messageServer,String id) {
		this.id=id;
		this.messageServer=messageServer;
		this.sessions=new ConcurrentHashMap<>();
		createTime=System.currentTimeMillis();
		autoRemoveDisconnectedSession=false;
	}
	//--------------------------------------------------------------------------
	//public interface
	/**
	 * return id of this channel
	 */
	public String getId(){
		return id;
	}
	/**
	 * add session to this channel
	 */
	public void addSession(Session session){
		if(session.principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		session.enterChannel(this);
		sessions.put(session.principal,session);
	}
	/** 
	 *remove session from channel by session's principal
	 */
	public Session removeSession(String principal){
		Session s=sessions.remove(principal);
		if(s!=null){
			s.leaveChannel(this);
		}
		return s;
	}
	/** 
	 *remove session from channel
	 */
	public void removeSession(Session session){
		sessions.remove(session.getPrincipal());
		session.leaveChannel(this);
	}
	/**
	 *remove all session from channel.
	 */
	public List<Session>removeAllSessions(){
		List<Session>allSessions=new ArrayList<>(sessions.values());
		allSessions.forEach(s->s.leaveChannel(this));
		sessions.clear();
		return allSessions;
	}
	/**
	 *get all session in this channel.
	 */
	public List<Session>getSessions(){
		List<Session>allSessions=new ArrayList<>(sessions.values());
		return allSessions;
	}
	/** 
	 *get session by principal
	 */
	public Session getSessionByPrincipal(String principal){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		return sessions.get(principal);
	}
	/**
	 *destroy this channel.
	 */
	public List<Session> destroy(){
		List<Session>result=removeAllSessions();
		messageServer.removeChannelInternal(id);
		return result;
	}
	/**
	 *@return channel create time.
	 */
	public long getCreateTime(){
		return createTime;
	}
	/** 
	 *broadcast message to all sessions in this channel.
	 */
	public void broadcast(String serviceId,Object payload){
		sessions.values().forEach(s->{
				s.push(serviceId, payload);
		});
	}
	/**
	 *broadcast message to all sessions in this channel expect session in blockPrincipalSet.
	 */
	public void broadcast(String serviceId,Object payload,Set<String>blockPrincipalSet){
		sessions.values().forEach(s->{
			if(!blockPrincipalSet.contains(s.principal)){
				s.push(serviceId, payload);
			}
		});
	}
	/** 
	 *@return user object 
	 */
	public Object getUserObject() {
		return userObject;
	}
	/** 
	 *set user object
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	/**
	 * @return is auto remove disconnnected session
	 */
	public boolean isAutoRemoveDisconnectedSession() {
		return autoRemoveDisconnectedSession;
	}
	/**
	 * 
	 * @param autoRemoveDisconnectedSession
	 */
	public void setAutoRemoveDisconnectedSession(
			boolean autoRemoveDisconnectedSession) {
		this.autoRemoveDisconnectedSession = autoRemoveDisconnectedSession;
	}
	
}
