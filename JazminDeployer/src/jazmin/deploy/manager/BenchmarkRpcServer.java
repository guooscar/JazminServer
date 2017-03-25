/**
 * 
 */
package jazmin.deploy.manager;

import jazmin.server.rpc.RpcClient;
import jazmin.server.rpc.RpcMessage;
import jazmin.server.rpc.RpcSession;

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
	private static final String DEFAULT_PRINCIPAL="JazminDeployer RpcServerRobot";
	RpcClient client;
	RpcSession rpcSession;
	//
	public BenchmarkRpcServer(BenchmarkSession session) {
		this.session=session;
		client=new RpcClient();
		client.setPrincipal(DEFAULT_PRINCIPAL);
	}
	//
	public void connect(String host,int port) throws Exception{
		connect(host, port, null, null, false);
	}
	//
	public void connect(String host,int port,String cluster,String credential,
		boolean enableSSL) throws Exception{
		rpcSession=new RpcSession();
		rpcSession.setRemoteHostAddress(host);
		rpcSession.setRemotePort(port);
		rpcSession.setCluster(cluster);
		rpcSession.setPrincipal(DEFAULT_PRINCIPAL);
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
	public RpcMessage invoke(
			String serviceId,
			Object[] args){
		return (RpcMessage) sample(serviceId, ()->{
			return client.invokeSync(rpcSession, serviceId, args);
		});
	}
}