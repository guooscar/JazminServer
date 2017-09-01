/**
 * 
 */
package jazmin.test.server.web;

import java.awt.Point;
import java.lang.reflect.Proxy;

import jazmin.core.Jazmin;
import jazmin.core.app.AppException;
import jazmin.server.web.WebApplication;
import jazmin.server.web.WebServer;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.ProxyController;

/**
 * @author g2131
 *
 */

public class TestProxyController extends WebApplication{
	public TestProxyController() {
		super("/srv/*");
	}
	//
	@Override
	public void init() throws Exception {
		super.init();
		registerController(new SimpleProxyController());
	}
	//
	@Controller(id="proxy")
	public static class SimpleProxyController extends ProxyController{
		public SimpleProxyController() {
			super();
			
			registerProxyTarget(new SimpleObject());
		}
	}
	//
	public static interface SimpleInterface{
		void test();
	}
	//
	public static class SimpleObject{
		public String echo(String str){
			return "server:"+str;
		}
		public Point add(Point a,Point b){
			return new Point(a.x+b.x,a.y+b.y);
		}
		public void error1() throws AppException{
			throw new AppException(100,"100error");
		}
		public void error2(){
			throw new IllegalArgumentException("error2");
		}
	}
	//
	public static void main(String[] args) {
		WebServer ws=new WebServer();
		ws.addApplication("/","/");
		Jazmin.addServer(ws);
		Jazmin.loadApplication(new TestProxyController());
		Jazmin.start();
	}
	
}
