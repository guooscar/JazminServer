/**
 * 
 */
package jazmin.test.server.message;

import jazmin.server.msg.Context;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class TestService {
	
	public void testMethod(Context c,String s1,Integer s2){
		c.ret(1111);
	}

}
