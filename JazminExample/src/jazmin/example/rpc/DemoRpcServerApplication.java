/**
 * 
 */
package jazmin.example.rpc;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RpcServer;

/**
 * @author yama
 *
 */
public class DemoRpcServerApplication extends Application{
	@Override
	public void init() throws Exception {
		super.init();
		RpcServer rpcServer=Jazmin.getServer(RpcServer.class);
		rpcServer.registerService(createWired(DemoRpcServiceImpl.class));
		//
		JobTasks jobTasks=new JobTasks();
		Jazmin.taskStore.registerTask(jobTasks);
		Jazmin.jobStore.registerJob(jobTasks);
	}
	//
	//--------------------------------------------------------------------------
	public static void main(String[] args) {
		LoggerFactory.setLevel("ALL");
		RpcServer rpcServer=new RpcServer();
		Jazmin.addServer(rpcServer);
		Jazmin.loadApplication(new DemoRpcServerApplication());
		Jazmin.start();
	}
}
