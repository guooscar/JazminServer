/**
 * 
 */
package jazmin.deploy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
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
@WebServlet(
		value = "/deployer/*",
		asyncSupported = true,
		loadOnStartup=1,
initParams={@WebInitParam(name="org.atmosphere.websocket.maxIdleTime",value="100000")})
@VaadinServletConfiguration(
        productionMode = true,
        ui = DeploySystemUI.class,
        widgetset="jazmin.deploy.AppWidgetSet")
public class DeployStartServlet extends VaadinServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public void init() throws ServletException {
		super.init();
		//
		try {
			DeployManager.setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DeployManager.reload();
	}
	//
	public static void main(String[] args) throws Exception{
		WebServer ws=new WebServer();
		Jazmin.environment.put("deploy.workspace","./workspace/");
		Jazmin.environment.put("deploy.hostname","10.44.218.119");
		ws.addResource("/","release/JazminDeployer");
		Jazmin.addServer(ws);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
