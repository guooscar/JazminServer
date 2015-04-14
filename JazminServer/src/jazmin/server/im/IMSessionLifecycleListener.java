package jazmin.server.im;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface IMSessionLifecycleListener {
	void sessionCreated(IMSession session)throws Exception;
	void sessionDisconnected(IMSession session)throws Exception;
}
