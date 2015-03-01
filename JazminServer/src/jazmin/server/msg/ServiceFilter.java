package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface ServiceFilter {
	void before(Context ctx)throws Exception;
	void after(Context ctx,Throwable e)throws Exception;
}
