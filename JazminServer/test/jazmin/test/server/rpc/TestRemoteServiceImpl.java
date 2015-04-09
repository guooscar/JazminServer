/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.util.RandomUtil;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TestRemoteServiceImpl implements TestRemoteService{
	static StringBuilder sb=new StringBuilder();
	static String s=null;
	static{
		for(int i=0;i<1000;i++){
			sb.append(RandomUtil.randomInt(10000));
		}
		s=sb.toString();
	}
	//
	@Override
	public String methodA() {
		return "12212121";
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
