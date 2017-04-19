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
		ResponseMessage run()throws Exception;
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
	private ResponseMessage sample(String name,SampleAction action){
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
	public String invoke(
			String serviceId,
			String[] args){
		ResponseMessage rsp=sample(serviceId, ()->{
			return client.invokeSync(serviceId, args);
		});
		if(rsp==null||rsp.responseObject==null){
			return null;
		}
		if(rsp.statusCode!=0){
			throw new IllegalArgumentException(rsp.statusMessage);
		}
		return (String) rsp.responseObject;
	}
}