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
	void methodA(AsyncCallback<String>callback);
	void timeoutMethod(long timeout,AsyncCallback<Void>callback);
}
