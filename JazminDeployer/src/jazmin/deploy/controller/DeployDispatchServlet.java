/**
 * 
 */
package jazmin.deploy.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import jazmin.server.web.mvc.DispatchServlet;

/**
 * @author yama
 * 6 Jan, 2015
 */
@WebServlet(value = "/srv/*",asyncSupported = true,loadOnStartup=0)
@SuppressWarnings("serial")
public class DeployDispatchServlet extends DispatchServlet{
	@Override
	public void init() throws ServletException {
		super.init();
		dispatcher.registerController(new DeployController());
	}
}
