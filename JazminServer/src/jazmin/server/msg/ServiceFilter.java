package jazmin.server.msg;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface ServiceFilter {
	/**
	 * before service invoke
	 * @param ctx
	 * @throws Exception
	 */
	void before(Context ctx)throws Exception;
	/**
	 * after service invoke
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	void after(Context ctx,Throwable e)throws Exception;
}
