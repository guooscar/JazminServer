package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public abstract class SessionLifecycleAdapter implements SessionLifecycleListener {
	@Override
	public void sessionCreated(Session session)throws Exception {}
	@Override
	public void sessionDisconnected(Session session) throws Exception{}
	@Override
	public void sessionDestroyed(Session session)throws Exception {}
}
