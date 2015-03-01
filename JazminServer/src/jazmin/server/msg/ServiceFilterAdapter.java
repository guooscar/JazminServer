package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public abstract class ServiceFilterAdapter implements ServiceFilter{
	public void before(Context ctx)throws Exception{}
	public void after(Context ctx,Throwable e){}
}
