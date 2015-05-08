/**
 * 
 */
package jazmin.core.thread;

import java.lang.reflect.Method;

/**
 * @author yama
 * 23 Dec, 2014
 */
public interface DispatcherCallback {
	public void before(Object instance, Method method, Object[] args)throws Exception;
	public void after(Object instance, Method method,Object[] args);
	public void end(Object instance, Method method, Object[] args,Object ret, Throwable e);
}
