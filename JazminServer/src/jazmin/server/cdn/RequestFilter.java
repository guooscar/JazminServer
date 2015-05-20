/**
 * 
 */
package jazmin.server.cdn;

/**
 * @author yama
 *
 */
public interface RequestFilter {
	void filter(FilterContext ctx)throws Exception;
	void endDownload(FilterContext ctx)throws Exception;
	void endUpload(FilterContext ctx)throws Exception;

}
