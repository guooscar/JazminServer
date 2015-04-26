package jazmin.test.server.stun;

import jazmin.core.Jazmin;
import jazmin.server.stun.StunServer;

/**
 * 
 * @author yama
 * 25 Apr, 2015
 */
public class StunServerTest {
	//
	public static void main(String[] args) {
		StunServer ss=new StunServer();
		ss.setSecondaryAddress("192.168.3.100");
		Jazmin.addServer(ss);
		Jazmin.start();
	}

}
