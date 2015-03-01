package jazmin.driver.rpc;

import java.lang.reflect.Method;

import jazmin.core.Jazmin;
import jazmin.core.aop.Dispatcher;
import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RPCMessage;
import jazmin.server.rpc.RPCMessage.AppExceptionMessage;
import jazmin.server.rpc.RPCMessageCallback;
import jazmin.server.rpc.RPCSession;
/**
 * 
 * @author yama
 * 16 Jan, 2015
 */
public class AsyncInvokeMessageCallback implements RPCMessageCallback {
	private static Logger logger=LoggerFactory.get(AsyncInvokeMessageCallback.class);
	//
	public AsyncCallback<Object> callback;
	public Method callbackMethod;
	public String serviceId;
	public Method method;
	public JazminRPCDriver driver;
	private long startTime;
	//
	public AsyncInvokeMessageCallback() {
		startTime=System.currentTimeMillis();
	}
	//
	public void beforeCall(){
		if(logger.isDebugEnabled()){
			logger.debug(">invoke:{}",serviceId);
		}
	}
	//
	@Override
	public void callback(RPCSession session, RPCMessage msg) {
		Throwable e = null;
		if (msg.payloads[1] != null) {
			Object eo = msg.payloads[1];
			if (eo instanceof Throwable) {
				e = (Throwable) eo;
			}
			if (eo instanceof AppExceptionMessage) {
				AppExceptionMessage aem = (AppExceptionMessage) eo;
				AppException ae = new AppException(aem.code, aem.message);
				e = ae;
			}
		}
		int useTime = (int) (System.currentTimeMillis() - startTime);
		if (logger.isDebugEnabled()) {
			logger.debug("<invoke:{} time {}", serviceId, useTime);
		}
		if (callback != null) {
			Jazmin.dispatcher.invokeInPool(
					serviceId,
					callback,
					callbackMethod,
					Dispatcher.EMPTY_CALLBACK,
					msg.payloads[0],e);
		}
		driver.statMethod(method, e, useTime);
	}
}