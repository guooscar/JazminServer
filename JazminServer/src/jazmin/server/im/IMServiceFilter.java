package jazmin.server.im;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public interface IMServiceFilter {
	void before(IMContext ctx)throws Exception;
	void after(IMContext ctx,Throwable e)throws Exception;
}
