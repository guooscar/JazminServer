/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.server.rpc.RemoteService;

/**
 * @author yama
 * 25 Dec, 2014
 */
public interface TestRemoteService extends RemoteService{
	String methodA();
	void timeoutMethod();
}
