/**
 * 
 */
package jazmin.deploy.manager;

import jazmin.core.app.AppException;
import jazmin.server.rpc.RpcClient;
import jazmin.server.rpc.RpcException;
import jazmin.server.rpc.RpcMessage;
import jazmin.server.rpc.RpcSession;
import jazmin.server.rpc.RpcMessage.AppExceptionMessage;

/**
 * 
 * @author icecooly
 *
 */
public class BenchmarkRpcServer {
	static interface SampleAction{
		Object run()throws Exception;
	}
	//
	BenchmarkSession session;
	RpcClient client;
	RpcSession rpcSession;
	//
	public BenchmarkRpcServer(BenchmarkSession session) {
		this.session=session;
		client=new RpcClient();
	}
	//
	public void connect(String host,int port,String principal) throws Exception{
		connect(host, port,principal,null, null, false);
		session.log("connect host:"+host+",port:"+port);
	}
	//
	public void connect(String host,int port,String principal,String cluster,String credential,
		boolean enableSSL) throws Exception{
		rpcSession=new RpcSession();
		rpcSession.setRemoteHostAddress(host);
		rpcSession.setRemotePort(port);
		rpcSession.setCluster(cluster);
		rpcSession.setPrincipal(principal);
		rpcSession.setCredential(credential);
		rpcSession.setEnableSSL(enableSSL);
		client.connect(rpcSession);
	}
	//
	private Object sample(String name,SampleAction action){
		long start=System.currentTimeMillis();
		boolean error=false;
		try{
			return action.run();
		}catch (Exception e) {
			session.log(e.getMessage());
			error=true;
			return null;
		}finally {
			session.sample(name,System.currentTimeMillis()-start, error);
		}
	}
	//
	public Object invoke(
			String serviceId,
			Object[] args) throws Throwable{
		RpcMessage message=(RpcMessage) sample(serviceId, ()->{
			return client.invokeSync(rpcSession, serviceId, args);
		});
		if(message==null||message.payloads==null||message.payloads.length==0){
			return null;
		}
		Throwable e=null;
		if(message.payloads[1]!=null){
			Object oo=message.payloads[1];
			if(oo instanceof Throwable){
				e=(Throwable)oo;
			}
			if(oo instanceof AppExceptionMessage){
				AppExceptionMessage aem=(AppExceptionMessage)oo;
				AppException ae=new AppException(aem.code,aem.message);
				e= ae;
			}
		}
		if(e!=null){
			if(e instanceof RpcException){
				throw new RpcException(e.getMessage());
			}
			throw e;
		}
		return message.payloads[0];
	}
}