package jazmin.server.im;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public abstract class IMSessionLifecycleAdapter implements IMSessionLifecycleListener {
	@Override
	public void sessionCreated(IMSession session)throws Exception {}
	@Override
	public void sessionDisconnected(IMSession session) throws Exception{}
	@Override
	public void sessionDestroyed(IMSession session)throws Exception {}
}
