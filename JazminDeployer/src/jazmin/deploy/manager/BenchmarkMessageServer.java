/**
 * 
 */
package jazmin.deploy.manager;

import jazmin.server.msg.client.MessageClient;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * 
 * @author icecooly
 *
 */
public class BenchmarkMessageServer {
	static interface SampleAction{
		Object run()throws Exception;
	}
	//
	BenchmarkSession session;
	private MessageClient client;
	//
	public BenchmarkMessageServer(BenchmarkSession session) {
		this.session=session;
		client=new MessageClient();
	}
	//
	public void connect(String host,int port) throws Exception{
		client.connect(host,port);
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
	public ResponseMessage invoke(
			String serviceId,
			String[] args){
		return (ResponseMessage) sample(serviceId, ()->{
			return client.invokeSync(serviceId, args);
		});
	}
}