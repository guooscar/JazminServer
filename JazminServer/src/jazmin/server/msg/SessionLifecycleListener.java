package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface SessionLifecycleListener {
	/**
	 * invoke when session created
	 * @param session
	 * @throws Exception
	 */
	void sessionCreated(Session session)throws Exception;
	/**
	 * invoke when session destroyed
	 * @param session
	 * @throws Exception
	 */
	void sessionDisconnected(Session session)throws Exception;
}
