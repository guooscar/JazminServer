/**
 * 
 */
package jazmin.server.im;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class IMChannel {
	String id;
	Map<String,IMSession>sessions;
	long createTime;
	Object userObject;
	IMMessageServer messageServer;

	boolean autoRemoveDisconnectedSession;
	//
	IMChannel(IMMessageServer messageServer,String id) {
		this.id=id;
		this.messageServer=messageServer;
		this.sessions=new ConcurrentHashMap<>();
		createTime=System.currentTimeMillis();
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
	public void addSession(IMSession session){
		if(session.principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		session.enterChannel(this);
		sessions.put(session.principal,session);
	}
	/** 
	 *remove session from channel by session's principal
	 */
	public IMSession removeSession(String principal){
		IMSession s=sessions.remove(principal);
		if(s!=null){
			s.leaveChannel(this);
		}
		return s;
	}
	/** 
	 *remove session from channel
	 */
	public void removeSession(IMSession session){
		removeSession(session.getPrincipal());
	}
	/**
	 *remove all session from channel.
	 */
	public List<IMSession>removeAllSessions(){
		List<IMSession>allSessions=new ArrayList<>();
		sessions.forEach((a,s)->{
			s.leaveChannel(this);
			allSessions.add(s);
		});
		sessions.clear();
		return allSessions;
	}
	/**
	 *get all session in this channel.
	 */
	public List<IMSession>getSessions(){
		List<IMSession>allSessions=new ArrayList<>(sessions.values());
		return allSessions;
	}
	/** 
	 *get session by principal
	 */
	public IMSession getSessionByPrincipal(String principal){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		return sessions.get(principal);
	}
	/**
	 *destroy this channel.
	 */
	public List<IMSession> destroy(){
		List<IMSession>result=removeAllSessions();
		messageServer.removeChannelInternal(id);
		return result;
	}
	/**
	 *get channel create time.
	 */
	public long getCreateTime(){
		return createTime;
	}
	/** 
	 *broadcast message to all sessions in this channel.
	 */
	public void broadcast(byte bb[]){
		sessions.forEach((a,s)->{
			s.push(bb);
	});
	}
	/**
	 *broadcast message to all sessions in this channel expect session in blockPrincipalSet.
	 */
	public void broadcast(byte bb[],Set<String>blockPrincipalSet){
		sessions.forEach((a,s)->{
			if(!blockPrincipalSet.contains(s.principal)){
				s.push(bb);
			}
		});
	}
	/** 
	 *get user object 
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
