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
	 *remove all session from channel.
	 */
	public List<IMSession>removeAllSessions(){
		List<IMSession>allSessions=new ArrayList<>(sessions.values());
		allSessions.forEach(s->s.leaveChannel(this));
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
	public void destroy(){
		removeAllSessions();
		messageServer.removeChannelInternal(id);
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
		sessions.values().forEach(s->{
			if(s.isActive()){
				s.push(bb);
			}
		});
	}
	/**
	 *broadcast message to all sessions in this channel expect session in blockPrincipalSet.
	 */
	public void broadcast(byte bb[],Set<String>blockPrincipalSet){
		sessions.values().forEach(s->{
			if(s.isActive()){
				if(!blockPrincipalSet.contains(s.principal)){
					s.push(bb);
				}
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
}
