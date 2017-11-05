/**
 * 
 */
package jazmin.deploy.controller;

import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.OutputListener;
import jazmin.deploy.domain.User;
import jazmin.deploy.manager.DeployManager;
import jazmin.server.web.mvc.BeforeService;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.Service;

/**
 * @author yama
 * 6 Jan, 2015
 */
@Controller(id="app")
public class AppController {
	//
	@BeforeService
	public boolean beforeInvoke(Context ctx){
		String user=ctx.getString("user", true);
		String sign=ctx.getString("sign", true);
		User t=DeployManager.validate(user, sign);
		ctx.put("result",t!=null);
		ctx.view(new JsonView());
		return t!=null;
	}
	/**
	 *login
	 */
	@Service(id="login")
	public void login(Context c){
		c.put("user", DeployManager.getUser(c.getString("user")));
		c.view(new JsonView());
	}
	//
	@Service(id="applications")
	public void applications(Context c){
		c.put("list",DeployManager.getApplications());
		c.view(new JsonView());
	}
	//
	@Service(id="instances")
	public void instances(Context c){
		c.put("list",DeployManager.getInstances());
		c.view(new JsonView());		
	}
	//
	@Service(id="machines")
	public void machines(Context c){
		c.put("list",DeployManager.getMachines());
		c.view(new JsonView());
	}
	//
	@Service(id="deploy_plans")
	public void deployPlans(Context c){
		c.put("list",DeployManager.getScripts("deployplan"));
		c.view(new JsonView());
	}
	//
	@Service(id="compile_app")
	public void compileApp(Context c){
		String appId=c.getString("id");
		Application app=DeployManager.getApplicationById(appId);
		StringBuilder sb=new StringBuilder();
		int ret=DeployManager.compileApp(app, new OutputListener() {
			@Override
			public void onOutput(String s) {
				sb.append(s);
			}
		});
		c.put("success",ret);
		c.put("output", sb.toString());
		c.view(new JsonView());	
	}
	//
	@Service(id="stop_instance")
	public void stopInstance(Context c){
		String instanceId=c.getString("id");
		Instance instance=DeployManager.getInstance(instanceId);
		StringBuilder sb=new StringBuilder();
		try {
			String s=DeployManager.stopInstance(instance);
			sb.append(s);
		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		c.put("output", sb.toString());
		c.view(new JsonView());
	}
	//
	@Service(id="start_instance")
	public void startInstance(Context c){
		String instanceId=c.getString("id");
		Instance instance=DeployManager.getInstance(instanceId);
		StringBuilder sb=new StringBuilder();
		try {
			String s=DeployManager.startInstance(instance);
			sb.append(s);
		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		c.put("output", sb.toString());
		c.view(new JsonView());
	}
	//
	@Service(id="test_instance")
	public void testInstance(Context c){
		String instanceId=c.getString("id");
		Instance instance=DeployManager.getInstance(instanceId);
		DeployManager.testInstance(instance);
		c.put("isAlive",instance.isAlive);
		c.view(new JsonView());
	}
}
