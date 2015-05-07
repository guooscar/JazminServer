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
}
