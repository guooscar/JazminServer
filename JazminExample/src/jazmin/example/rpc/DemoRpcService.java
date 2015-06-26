/**
 * 
 */
package jazmin.example.rpc;

import jazmin.driver.rpc.AsyncCallback;

/**
 * @author yama
 *
 */
public interface DemoRpcService{
	String echo(String input);
	//
	//--------------------------------------------------------------------------
	public static interface DemoRpcServiceAsync {
		void echo(String input,AsyncCallback<String>callback);
	}
}
