/**
 * 
 */
package jazmin.example.rpc;

import jazmin.driver.rpc.AsyncCallback;
import jazmin.server.rpc.RemoteService;

/**
 * @author yama
 *
 */
public interface DemoRpcService extends RemoteService{
	String echo(String input);
	//
	//--------------------------------------------------------------------------
	public static interface DemoRpcServiceAsync {
		void echo(String input,AsyncCallback<String>callback);
	}
}
