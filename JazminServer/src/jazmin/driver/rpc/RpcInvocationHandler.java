package jazmin.driver.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RpcClient;
import jazmin.server.rpc.RpcException;
import jazmin.server.rpc.RpcSession;
/**
 * 
 * @author yama
 * 25 Dec, 2014
 */
public abstract class RpcInvocationHandler implements InvocationHandler {
	private static Logger logger=LoggerFactory.get(RpcInvocationHandler.class);
	//
	private int invokeCounter;
	private RpcSession[]sessions;
	private int sessionCount;
	protected RpcClient client;
	protected JazminRpcDriver driver;
	//
	public RpcInvocationHandler(
			JazminRpcDriver driver,
			RpcClient client,
			List<RpcSession> sessionList) {
		this.client=client;
		this.driver=driver;
		sessions=sessionList.toArray(new RpcSession[sessionList.size()]);
		invokeCounter=0;
		sessionCount=sessions.length;
	}
	/**
	 * 
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		//
		int idx=Math.abs(invokeCounter++%sessionCount);
		RpcSession session = sessions[idx];
		if(!session.isConnected()){
			//find next active session
			for(int i=0;i<sessionCount;i++){
				session=sessions[i];
				if(session.isConnected()){
					break;
				}
			}
		}
		//if all session deactive throw exception.
		if(!session.isConnected()){
			logger.warn("no active session for connection.{}:{}",
					session.getRemoteHostAddress(),
					session.getRemotePort());
			throw new RpcException("no active session for connection."+
					session.getRemoteHostAddress()+":"+
					session.getRemotePort());
		}
		return invoke0(session,proxy,method,args);
	}
	/**
	 * 调用rpcclient发送消息
	 */
	protected abstract Object invoke0(
			RpcSession session,
			Object proxy, 
			Method method, 
			Object[] args)throws Throwable;
}
