package jazmin.server.web;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.server.web.mvc.DispatchServlet;

/**
 * 
 * @author yama
 *
 */
public class WebApplication extends Application{
	private String dispatchContextPath;
	public WebApplication(String dispatchContext) {
		this.dispatchContextPath=dispatchContext;
	}
	/**
	 * init web application
	 */
	@Override
	public void init() throws Exception {
		super.init();
		//
		WebServer ws=Jazmin.getServer(WebServer.class);
		if(ws==null){
			throw new IllegalStateException("can not found WebServer");
		}
		//
		ws.addServlet(DispatchServlet.class,dispatchContextPath);
	}
	/**
	 * register new controller
	 * @param controller
	 */
	public void registerController(Object controller){
		DispatchServlet.dispatcher.registerController(controller);
	}
}
