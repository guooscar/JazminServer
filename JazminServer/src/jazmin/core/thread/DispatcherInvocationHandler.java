package jazmin.core.thread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author yama
 * @date Jun 4, 2014
 */
public class DispatcherInvocationHandler  implements InvocationHandler {
	private Object object;
	private Dispatcher dispatcher;
	DispatcherInvocationHandler(Object object,Dispatcher dispatcher){
		this.object=object;
		this.dispatcher=dispatcher;
	}
	//
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Method m=object.getClass().getMethod(method.getName(),method.getParameterTypes());
		ThreadWorker tw=dispatcher.invokeInCaller(
				"",object, m,
				Dispatcher.EMPTY_CALLBACK, args);
		if(tw.getException()!=null){
			throw tw.getException();
		}
		return tw.getRet();
	}
}
