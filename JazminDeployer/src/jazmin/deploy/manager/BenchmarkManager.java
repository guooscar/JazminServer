/**
 * 
 */
package jazmin.deploy.manager;

/**
 * @author yama
 *
 */
public class BenchmarkManager {
	//
	public BenchmarkSession startSession(){
		BenchmarkSession session=new BenchmarkSession();
		session.start(new BenchmarkRobot() {
			@Override
			public void start() throws Exception {
				System.out.println("start-"+Thread.currentThread().getName());
				
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
				BenchmarkRpc rpc=new BenchmarkRpc(session);
				rpc.url="http://139.199.176.211/Sztsg/Invoke";
				rpc.token="07a6cbb6bb864d2ca22940faf04fd596";
				rpc.invoke("GetUserMoneyLogs", "{'PageIndex':0,'PageSize':10}");
				System.out.println("loop-"+Thread.currentThread().getName());
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
