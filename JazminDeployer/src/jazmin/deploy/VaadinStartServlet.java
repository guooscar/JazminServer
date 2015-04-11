/**
 * 
 */
package jazmin.deploy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import jazmin.core.Jazmin;
import jazmin.deploy.domain.DeployManager;
import jazmin.server.console.ConsoleServer;
import jazmin.server.web.WebServer;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

/**
 * @author yama
 * 28 Dec, 2014
 */
@WebServlet(value = "/*",asyncSupported = true,loadOnStartup=1)
@VaadinServletConfiguration(
        productionMode = false,
        ui = DeploySystemUI.class)
public class VaadinStartServlet extends VaadinServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public void init() throws ServletException {
		super.init();
		//
		DeployManager.reload();
	}
	//
	public static void main(String[] args) throws Exception{
		WebServer ws=new WebServer();
		Jazmin.environment.put("deploy.config.path","workspace/config/");
		Jazmin.environment.put("deploy.template.path","workspace/template/");
		Jazmin.environment.put("deploy.package.path","workspace/package/");
		ws.addResource("/","release/JazminDeployer");
		Jazmin.addServer(ws);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
