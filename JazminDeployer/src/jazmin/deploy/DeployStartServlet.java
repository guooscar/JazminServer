/**
 * 
 */
package jazmin.deploy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.core.LifecycleAdapter;
import jazmin.deploy.manager.DeployManager;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.web.WebServer;
import jazmin.server.websockify.WebsockifyServer;
import jazmin.server.webssh.WebSshServer;

import org.apache.velocity.app.Velocity;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

/**
 * @author yama
 * 28 Dec, 2014
 */
@WebServlet(
		value = {"/deployer/*","/VAADIN/*"},
		asyncSupported = true,
		loadOnStartup=1)
//
@VaadinServletConfiguration(
        productionMode = false,
        ui = DeploySystemUI.class)
@SuppressWarnings("serial")
public class DeployStartServlet extends VaadinServlet{
	static{
		Velocity.init();
	}
	@Override
	public void init() throws ServletException {
		super.init();
		WebServer ws=Jazmin.getServer(WebServer.class);
		ws.setLifecycleListener(new LifecycleAdapter() {
			@Override
			public void afterStart(Lifecycle server) throws Exception {
				try {
					DeployManager.setup();
				} catch (Exception e) {
					e.printStackTrace();
				}
				DeployManager.reload();
			}
		});
	}
	//
	@Override
    protected final void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new DeploySessionInitListener());
    }
	//
	public static void main(String[] args) throws Exception{
		WebServer ws=new WebServer();
		/*Jazmin.environment.put("deploy.workspace","/Users/yama/Documents/JazminDeployerBackup/workspace/");
		Jazmin.environment.put("deploy.hostname","10.44.218.119");
		Jazmin.environment.put("deploy.ant","/usr/local/bin/ant");
		Jazmin.environment.put("deploy.repo.dir","/Users/yama/Desktop/svn_test");
		Jazmin.environment.put("deploy.ant.lib","/Users/yama/git/JazminServer/JazminServer/release/JazminServer");
		*/
		
		ws.addResource("/","release/JazminDeployer");
		LoggerFactory.setLevel("DEBUG");
		Jazmin.addServer(ws);
		Jazmin.addServer(new WebSshServer());
		Jazmin.addServer(new WebsockifyServer());
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
