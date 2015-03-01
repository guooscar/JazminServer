package jazmin.driver.rpc;

import java.lang.reflect.Method;
import java.util.List;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RPCClient;
import jazmin.server.rpc.RPCMessage;
import jazmin.server.rpc.RPCMessage.AppExceptionMessage;
import jazmin.server.rpc.RPCSession;

/**
 * @author yama
 * @date Jun 4, 2014
 */
public class SyncRPCInvocationHandler extends RPCInvocationHandler {
	private static Logger logger=LoggerFactory.get(SyncRPCInvocationHandler.class);
	//
	public SyncRPCInvocationHandler(
			JazminRPCDriver driver,
			RPCClient client,
			List<RPCSession> sessions) {
		super(driver,client,sessions);
	}
	//
	@Override
	protected Object invoke0(
			RPCSession session, 
			Object proxy, 
			Method method,
			Object[] args) throws Throwable {
		String serviceId=method.getDeclaringClass().getSimpleName()+"."+method.getName();
		long startTime=System.currentTimeMillis();
		if(logger.isDebugEnabled()){
			logger.debug(">invoke {}",serviceId);
		}
		RPCMessage msg=client.invokeSync(session, serviceId, args);
		int useTime= (int)(System.currentTimeMillis()-startTime);
		if(logger.isDebugEnabled()){
			logger.debug("<invoke {} time {}",serviceId,useTime);
		}
		Throwable e=null;
		if(msg.payloads[1]!=null){
			Object oo=msg.payloads[1];
			if(oo instanceof Throwable){
				e=(Throwable)oo;
			}
			if(oo instanceof AppExceptionMessage){
				AppExceptionMessage aem=(AppExceptionMessage)oo;
				AppException ae=new AppException(aem.code,aem.message);
				e= ae;
			}
		}
		driver.statMethod(method, e, useTime);
		if(e!=null){
			throw e;
		}
		return msg.payloads[0];
	}
}
