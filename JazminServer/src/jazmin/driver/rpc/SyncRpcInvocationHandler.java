package jazmin.driver.rpc;

import java.lang.reflect.Method;
import java.util.List;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RpcClient;
import jazmin.server.rpc.RpcException;
import jazmin.server.rpc.RpcMessage;
import jazmin.server.rpc.RpcMessage.AppExceptionMessage;
import jazmin.server.rpc.RpcSession;

/**
 * @author yama
 * @date Jun 4, 2014
 */
public class SyncRpcInvocationHandler extends RpcInvocationHandler {
	private static Logger logger=LoggerFactory.get(SyncRpcInvocationHandler.class);
	//
	public SyncRpcInvocationHandler(
			JazminRpcDriver driver,
			RpcClient client,
			List<RpcSession> sessions) {
		super(driver,client,sessions);
	}
	//
	@Override
	protected Object invoke0(
			RpcSession session, 
			Object proxy, 
			Method method,
			Object[] args) throws Throwable {
		String serviceId=method.getDeclaringClass().getSimpleName()+"."+method.getName();
		long startTime=System.currentTimeMillis();
		if(logger.isDebugEnabled()){
			logger.debug(">invoke {}",serviceId);
		}
		RpcMessage msg=client.invokeSync(session, serviceId, args);
		int useTime= (int)(System.currentTimeMillis()-startTime);
		int networkTime=(int)(msg.reveicedTime-msg.sentTime);
		if(logger.isDebugEnabled()){
			logger.debug("<invoke {} time {}-{}",serviceId,useTime,networkTime);
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
			if(e instanceof RpcException){
				throw new RpcException(e.getMessage());
			}
			throw e;
		}
		return msg.payloads[0];
	}
}
