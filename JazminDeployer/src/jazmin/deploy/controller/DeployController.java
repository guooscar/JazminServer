/**
 * 
 */
package jazmin.deploy.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import jazmin.core.Jazmin;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.ErrorView;
import jazmin.server.web.mvc.FileView;
import jazmin.server.web.mvc.PlainTextView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;

/**
 * @author yama
 * 6 Jan, 2015
 */
@Controller(id="deploy")
public class DeployController {
	//
	private static Logger logger=LoggerFactory.get(DeployController.class);

	//
	private boolean checkMachine(Context c,String instanceId){
		if(c.request().session(true).getAttribute("user")!=null){
			return true;
		}
		Instance instance=DeployManager.getInstance(instanceId);
		if(instance==null){
			return false;
		}
		if(instance.getProperties().containsKey(Instance.P_NO_CHECK_IP)){
			return true;
		}
		String remoteAddr=c.request().raw().getRemoteAddr();
		if(!instance.machine.publicHost.equals(remoteAddr)
				&&!instance.machine.privateHost.equals(remoteAddr)){
			logger.warn("addr check {} - {} - {}",
					instance.machine.publicHost,
					instance.machine.privateHost,
					remoteAddr);	
			c.view(new ErrorView(HttpServletResponse.SC_FORBIDDEN,"bad machine host"));
			return false;
		}
		return true;
	}
	//
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
		if(!checkMachine(c,instanceName)){
			return;
		}
		//
		String result=DeployManager.renderTemplate(instanceName);
		if(result!=null){
			c.view(new PlainTextView(result));
		}else{
			c.view(new PlainTextView("can not find template for instance:"+instanceName));	
		}
	}
	//
	@Service(id = "pkg")
	public void downloadInstancePackage(Context c) {
		List<String> querys = c.request().querys();
		if (querys.size() < 3) {
			return;
		}
		String instanceId = querys.get(2);
		Instance instance=DeployManager.getInstance(instanceId);
		if(!checkMachine(c,instanceId)){
				return;
		}
		jazmin.deploy.domain.AppPackage result = DeployManager
				.getInstancePackage(instanceId);
		if (result != null) {
			c.view(new PackageDownloadView(instance,result));
		}
	}
	//--------------------------------------------------------------------------
	@Service(id="instance_taillog",queryCount=3)
	public void instanceTailLog(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		String instanceId=querys.get(2);
		Instance instance=DeployManager.getInstance(instanceId);
		if(instance==null){
			c.view(new ErrorView(404));
			return;
		}
		Machine machine=instance.machine;
		c.put("token",DeployManager.createOneTimeSSHToken(
				machine,
				false,
				false,
				"tail -f "+
				instance.machine.jazminHome+"log/"+
				instance.id+".log"));
		c.view(new ResourceView("/jsp/webssh.jsp"));
	}
	//
	@Service(id="instance_webssh",queryCount=3)
	public void instanceWebssh(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		String instanceId=querys.get(2);
		Instance instance=DeployManager.getInstance(instanceId);
		if(instance==null){
			c.view(new ErrorView(404));
			return;
		}
		Machine machine=instance.machine;
		Machine fakeMachine=new Machine();
		Map<String,String>p=instance.properties;
		fakeMachine.publicHost=machine.publicHost;
		fakeMachine.sshUser=p.getOrDefault(Instance.P_JAZMIN_CONSOLE_USER, "jazmin");
		fakeMachine.sshPassword=p.getOrDefault(Instance.P_JAZMIN_CONSOLE_PWD, "jazmin");
		fakeMachine.sshPort=instance.port+10000;
		c.put("token",DeployManager.createOneTimeSSHToken(fakeMachine,false,true,null));
		c.view(new ResourceView("/jsp/webssh.jsp"));
	}
	//
	@Service(id="webssh",queryCount=3)
	public void webssh(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		String machineId=querys.get(2);
		Machine machine=DeployManager.getMachine(machineId);
		if(machine==null){
			c.view(new ErrorView(404));
			return;
		}
		c.put("token",DeployManager.createOneTimeSSHToken(machine,false,true,null));
		c.view(new ResourceView("/jsp/webssh.jsp"));
	}
	//
	@Service(id="rootwebssh",queryCount=3)
	public void rootwebssh(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		String machineId=querys.get(2);
		Machine machine=DeployManager.getMachine(machineId);
		if(machine==null){
			c.view(new ErrorView(404));
			return;
		}
		c.put("token",DeployManager.createOneTimeSSHToken(machine,true,true,null));
		c.view(new ResourceView("/jsp/webssh.jsp"));
	}
	//
	@Service(id="sysgraph")
	public void getSystemGraph(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String systemName=querys.get(2);
		String result=DeployManager.renderApplicationGraph(systemName);
		c.put("dot_string",result);
		c.view(new ResourceView("/jsp/graph.jsp"));
	}
	//
	@Service(id="insgraph")
	public void getInstanceGraph(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		if(querys.size()<4){
			return;
		}
		String systemName=querys.get(2);
		String clusterName=querys.get(3);
		String result=DeployManager.renderInstanceGraph(systemName,clusterName);
		c.put("dot_string",result);
		c.view(new ResourceView("/jsp/graph.jsp"));
	}
	//
	@Service(id="download")
	public void downloadPackage(Context c){
		if(!checkMachine(c,"")){
			return;
		}
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String packageName=querys.get(2);
		jazmin.deploy.domain.AppPackage result=DeployManager.getPackage(packageName);
		if(result!=null){
			c.view(new FileView(result.file));
		}
	}	
	//
	@Service(id="download_jazmin")
	public void downloadJazmin(Context c){
		String path=Jazmin.environment.getString("deploy.ant.lib","");
		File jazminFile=new File(path,"jazmin.jar");
		if(jazminFile.exists()){
			c.view(new FileView(jazminFile.getAbsolutePath()));
		}
	}
}
