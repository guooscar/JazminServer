/**
 * 
 */
package jazmin.driver.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import jazmin.core.aop.Dispatcher;
import jazmin.core.aop.ThreadWorker;

/**
 * @author yama
 * 22 Jan, 2015
 */
public class ProxyInvocationHandler implements InvocationHandler{
	private Dispatcher dispatcher;
	private Object targetObject;
	public ProxyInvocationHandler(Dispatcher dispatcher,Object targetObject) {
		this.dispatcher=dispatcher;
		this.targetObject=targetObject;
	}
	//
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		ThreadWorker tw=dispatcher.invokeInCaller("", targetObject, method, 
				Dispatcher.EMPTY_CALLBACK, args);
		if(tw.getException()!=null){
			throw tw.getException();
		}
		return tw.getRet();
	}
}
