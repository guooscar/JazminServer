package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public abstract class ServiceFilterAdapter implements ServiceFilter{
	/**
	 *before service invoke 
	 */
	public void before(Context ctx)throws Exception{}
	/**
	 * after service invoke
	 */
	public void after(Context ctx,Throwable e){}
}
