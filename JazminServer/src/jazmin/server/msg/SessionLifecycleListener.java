package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface SessionLifecycleListener {
	void sessionCreated(Session session)throws Exception;
	void sessionDisconnected(Session session)throws Exception;
	void sessionDestroyed(Session session)throws Exception;
}
