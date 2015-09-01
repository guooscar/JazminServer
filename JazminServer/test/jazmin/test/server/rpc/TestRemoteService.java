/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.util.RandomUtil;


/**
 * @author yama
 * 25 Dec, 2014
 */
public interface TestRemoteService {
	String methodA(String input);
	void timeoutMethod(long sleep);
	public static class TestRemoteServiceImpl implements TestRemoteService{
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
			public String methodA(String input) {
				return input;
			}
			//
			@Override
			public void timeoutMethod(long sleep) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	}
}
