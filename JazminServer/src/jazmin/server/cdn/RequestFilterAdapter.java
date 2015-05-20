/**
 * 
 */
package jazmin.server.cdn;

/**
 * @author yama
 *
 */
public abstract class RequestFilterAdapter implements RequestFilter{
	public void filter(FilterContext ctx)throws Exception{}
	public void endUpload(FilterContext ctx)throws Exception{}
	public void endDownload(FilterContext ctx)throws Exception{}
}
