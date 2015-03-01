package jazmin.test.server.web;

import jazmin.server.web.WebServer;

public class WebServerTest {

	//
	//
    public static void main(String[] args) throws Exception {
    	WebServer webServer=new WebServer();
    	webServer.addWar("/", "/Users/yama/Desktop/GenCode.war");
//    	webServer.addResource("/", "GenCode");
    	webServer.init();
    	webServer.start();
    }
}
