/**
 * 
 */
package jazmin.test.server.web;

import jazmin.server.web.mvc.ProxyController;

/**
 * @author yama
 *
 */
public class TestProxyControllerProxy {

	static interface BxcAction{
		void getGoods(String token,int u);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BxcAction action=ProxyController.create(BxcAction.class,"https://bxc.uat3.itit.io/p/client/invoke");
		action.getGoods("",51);
	}

}
