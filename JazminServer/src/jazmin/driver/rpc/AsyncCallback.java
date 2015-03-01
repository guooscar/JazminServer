/**
 * 
 */
package jazmin.driver.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
@FunctionalInterface
public interface AsyncCallback<T> {
	void callback(T t,Throwable e)throws Exception;
}
