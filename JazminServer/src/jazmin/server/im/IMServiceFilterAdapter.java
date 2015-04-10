package jazmin.server.im;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public abstract class IMServiceFilterAdapter implements IMServiceFilter{
	public void before(IMContext ctx)throws Exception{}
	public void after(IMContext ctx,Throwable e){}
}
