/**
 * 
 */
package jazmin.test.server.rpc;

import jazmin.server.rpc.RemoteService;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TestInterface {
	public static void main(String[] args) {
		TestRemoteServiceImpl srv=new TestRemoteServiceImpl();
		System.out.println(srv.getClass());
		Class<?>interfaceClass=null;
		for(Class<?>cc:srv.getClass().getInterfaces()){
			if(RemoteService.class.isAssignableFrom(cc)){
				//interface is subclass of RemoteService
				interfaceClass=cc;
			}
		}
		System.out.println(interfaceClass);
	}
}
