package jazmin.driver.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RPCClient;
import jazmin.server.rpc.RPCException;
import jazmin.server.rpc.RPCSession;
/**
 * 
 * @author yama
 * 25 Dec, 2014
 */
public abstract class RPCInvocationHandler implements InvocationHandler {
	private static Logger logger=LoggerFactory.get(RPCInvocationHandler.class);
	//
	private int invokeCounter;
	private RPCSession[]sessions;
	private int sessionCount;
	protected RPCClient client;
	protected JazminRPCDriver driver;
	//
	public RPCInvocationHandler(
			JazminRPCDriver driver,
			RPCClient client,
			List<RPCSession> sessionList) {
		this.client=client;
		this.driver=driver;
		sessions=sessionList.toArray(new RPCSession[sessionList.size()]);
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
		RPCSession session = sessions[idx];
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
			throw new RPCException("no active session for connection."+
					session.getRemoteHostAddress()+":"+
					session.getRemotePort());
		}
		return invoke0(session,proxy,method,args);
	}
	/**
	 * 调用rpcclient发送消息
	 */
	protected abstract Object invoke0(
			RPCSession session,
			Object proxy, 
			Method method, 
			Object[] args)throws Throwable;
}
