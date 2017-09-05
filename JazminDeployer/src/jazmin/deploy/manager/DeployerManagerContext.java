/**
 * 
 */
package jazmin.deploy.manager;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;

/**
 * @author yama
 *
 */
public interface DeployerManagerContext {
	public void compile(String appId);
	public void start(String instanceId);
	public void startAndWait(String instanceId,int counter);
	public void restartAndWait(String instanceId,int time);
	public void stop(String instanceId);
	public List<Application> getApplications(String query);
	public List<Instance> getInstances(String query) ;
	public List<Machine> getMachines(String query);
	//
	public String getVar(String key);
	public void log(String info);
	//
	//-------------------------------------------------------------------------
	public static interface OutputHandler{
		void onOutput(String out);
	}
	//
	public  class DeployerManagerContextContextImpl implements DeployerManagerContext{
		OutputHandler handler;
		Map<String,String>vars;
		public DeployerManagerContextContextImpl(OutputHandler handler,Map<String,String>vars) {
			this.handler=handler;
			this.vars=vars;
		}
		//
		private void info(String info){
			handler.onOutput("\n*********************************************\n");
			handler.onOutput(info+"\n");
			handler.onOutput("*********************************************\n");
		}
		//
		public void compile(String appId){
			Application app=DeployManager.getApplicationById(appId);
			if(app==null){
				handler.onOutput("can not find application "+appId+"\n");
				return;
			}
			info("compile "+appId);
			int ret=DeployManager.compileApp(app, handler::onOutput);
			if(ret==0){
				info("compile success");
			}else{
				info("compile fail");
			}
		}
		//
		public void start(String instanceId){
			Instance instance=DeployManager.getInstanceById(instanceId);
			if(instance==null){
				handler.onOutput("can not find instance "+instanceId+"\n");
				return;
			}
			info("start instance:"+instanceId);
			try {
				DeployManager.startInstance(instance);
			} catch (Exception e) {
				info(e.getMessage());
			}
		}
		//
		public void startAndWait(String instanceId,int counter){
			start(instanceId);
			Instance instance=DeployManager.getInstanceById(instanceId);
			while(counter-->0){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				handler.onOutput("wait for instance:"+instanceId+" "+counter+"\n");
				DeployManager.testInstance(instance);
				if(instance.isAlive){
					break;
				}
			}
			if (!instance.isAlive) {
				info(instance.id + " not response after 30 seconds");
			} else {
				info(instance.id + " start success");
			}
		}
		//
		public void stop(String instanceId){
			Instance instance=DeployManager.getInstanceById(instanceId);
			if(instance==null){
				handler.onOutput("can not find instance "+instanceId+"\n");
				return;
			}
			info("stop instance:"+instanceId);
			try {
				DeployManager.stopInstance(instance);
			} catch (Exception e) {
				info("stop instance error"+e.getMessage());
			}
		}
		//
		public void restartAndWait(String instanceId,int time){
			stop(instanceId);
			startAndWait(instanceId, time);
		}
		//
		public List<Machine> getMachines(String query) {
			return DeployManager.getMachines(DeploySystemUI.getUser().id,query);
		}
		//
		public List<Instance> getInstances(String query) {
			return DeployManager.getInstances(DeploySystemUI.getUser().id,query);
		}
		//
		public List<Application> getApplications(String query) {
			return DeployManager.getApplications(DeploySystemUI.getUser().id,query);
		}
		//
		public void run(String name,String source)throws ScriptException{
			info("run deploy plan:"+name);
			ScriptEngineManager engineManager = new ScriptEngineManager();
			ScriptEngine engine = engineManager.getEngineByName("nashorn");
			SimpleScriptContext ssc=new SimpleScriptContext();
			ssc.setAttribute("deployer", this,ScriptContext.ENGINE_SCOPE);
			Bindings bindings = engine.createBindings();
			bindings.put("deployer", this);
			engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			
			String commonScript=
						"load('nashorn:mozilla_compat.js');"+
						"importPackage(Packages.jazmin.deploy.manager);\n";
			engine.eval(commonScript+source, ssc); 
			info("run deploy plan:"+name+" complete");
		}
		//
		@Override
		public String getVar(String key) {
			return vars.get(key);
		}
		//
		@Override
		public void log(String info) {
			info(info);
		}
	}
}
