/**
 * 
 */
package jazmin.test.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TestRemoteServiceImpl implements TestRemoteService{
	//
	@Override
	public int methodA() {
		return 100;
	}
	//
	@Override
	public void timeoutMethod() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
