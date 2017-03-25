/**
 * 
 */
package jazmin.deploy.manager;

import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;

/**
 * @author yama
 *
 */
public class BenchmarkManager {
	//
	public BenchmarkSession startSession(){
		BenchmarkSession session=new BenchmarkSession();
		//BenchmarkRpcServer rpc=new BenchmarkRpcServer(session);
		BenchmarkMessageServer messageServer=new BenchmarkMessageServer(session);
		session.start(new BenchmarkRobot() {
			@Override
			public void start() throws Exception {
				System.out.println("start-"+Thread.currentThread().getName());
				messageServer.connect("uat.itit.io", 8602);
				messageServer.invoke("ZjhService.loginByPassword",new String[]{"90","57c23484a8b8991e8eb05371cb39792d","127.0.1.1","Robot"});
			}
			@Override
			public String name() {
				return "Test";
			}
			//
			@Override
			public void loop() throws Exception {
//				BenchmarkHttp http=new BenchmarkHttp(session);
//				http.post("http://www.baidu.com");
//				System.out.println("loop-"+Thread.currentThread().getName());
//				Thread.sleep(1000);
				//
//				rpc.invoke("ZjhAction.loginByPassword",new Object[]{90,"57c23484a8b8991e8eb05371cb39792d","127.0.1.1","Robot"});
//				System.out.println("loop-"+Thread.currentThread().getName());
//				Thread.sleep(1000);
				//
				ResponseMessage rsp=messageServer.invoke("ZjhService.getTableInfo",null);
				System.out.println("loop-"+Thread.currentThread().getName()+"/"+DumpUtil.dump(rsp));
				Thread.sleep(1000);
			}
			//
			@Override
			public void end() throws Exception {
				System.out.println("end-"+Thread.currentThread().getName());
				
			}
		}, 10, 10, 10);
		return session;
	}
	//
	public static void main(String[] args) {
		BenchmarkManager manager=new BenchmarkManager();
		manager.startSession();
	}
}
