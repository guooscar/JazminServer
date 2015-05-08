/**
 * 
 */
package jazmin.core.thread;

import java.lang.reflect.Method;

/**
 * @author yama
 * 23 Dec, 2014
 */
public abstract class DispatcherCallbackAdapter implements DispatcherCallback{

	@Override
	public void before(Object instance, Method method, Object[] args)throws Exception {}
	@Override
	public void after(Object instance, Method method, Object[] args) {}
	@Override
	public void end(Object instance, Method method, Object[] args, Object ret,
			Throwable e) {}
}
