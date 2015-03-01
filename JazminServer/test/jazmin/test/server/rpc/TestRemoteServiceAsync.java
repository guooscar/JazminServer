/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.driver.rpc.AsyncCallback;

/**
 * @author yama
 * 25 Dec, 2014
 */
public interface TestRemoteServiceAsync{
	void methodA(AsyncCallback<Integer>callback);
	void timeoutMethod(AsyncCallback<Void>callback);
}
