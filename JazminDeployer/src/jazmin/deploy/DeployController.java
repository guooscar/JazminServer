/**
 * 
 */
package jazmin.deploy;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Instance;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.ErrorView;
import jazmin.server.web.mvc.FileView;
import jazmin.server.web.mvc.PlainTextView;
import jazmin.server.web.mvc.Service;

/**
 * @author yama
 * 6 Jan, 2015
 */
@Controller(id="deploy")
public class DeployController {
	/**
	 *get boot file 
	 */
	@Service(id="boot")
	public void getBootFile(Context c){
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String instanceName=querys.get(2);
		String result=DeployManager.renderTemplate(instanceName);
		if(result!=null){
			c.view(new PlainTextView(result));
		}else{
			c.view(new PlainTextView("can not find template for instance:"+instanceName));	
		}
	}
	//
	@Service(id="log")
	public void getTailLog(Context c){
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String instanceName=querys.get(2);
		String result=DeployManager.getTailLog(instanceName);
		if(result!=null){
			c.view(new PlainTextView(result));
		}
	}
	//
	@Service(id="report")
	public void getActionReport(Context c){
		c.view(new PlainTextView(DeployManager.actionReport()));
	}
	//
	@Service(id="download")
	public void downloadPackage(Context c){
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String packageName=querys.get(2);
		jazmin.deploy.domain.Package result=DeployManager.getPackage(packageName);
		if(result!=null){
			c.view(new FileView(result.file));
		}
	}

	@Service(id = "pkg")
	public void downloadInstancePackage(Context c) {
		List<String> querys = c.request().querys();
		if (querys.size() < 3) {
			return;
		}
		String instanceId = querys.get(2);
		Instance instance=DeployManager.instance(instanceId);
		if(instance==null){
			return;
		}
		String remoteAddr=c.request().raw().getRemoteAddr();
		if(!instance.machine.publicHost.equals(remoteAddr)
				||!instance.machine.privateHost.equals(remoteAddr)){
			c.view(new ErrorView(HttpServletResponse.SC_FORBIDDEN));
			return ;
		}
		jazmin.deploy.domain.Package result = DeployManager
				.getInstancePackage(instanceId);
		if (result != null) {
			c.view(new FileView(result.file));
		}
	}
}
