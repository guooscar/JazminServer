/**
 * 
 */
package jazmin.deploy.manager;

import java.io.IOException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jazmin.deploy.workflow.WorkflowEngine;
import jazmin.deploy.workflow.definition.WorkflowProcess;
import jazmin.deploy.workflow.execute.EventHandler;
import jazmin.deploy.workflow.execute.ExceptionHandler;
import jazmin.deploy.workflow.execute.ProcessInstance;

/**
 * @author yama
 *
 */
public interface RobotWorkflowEngineContext {
	public ProcessInstance startWorkflow(String name,EventHandler handler,ExceptionHandler errorHandler);
	public void attachWorkflow(ProcessInstance instance);
	public void detachWorkflow(String id);
	
	//
	public  class RobotWorkflowEngineContextImpl implements RobotWorkflowEngineContext{
		WorkflowEngine engine;
		public RobotWorkflowEngineContextImpl(WorkflowEngine engine) {
			this.engine=engine;
		}
		//
		public ProcessInstance startWorkflow(String name,EventHandler handler,ExceptionHandler errorHandler){
			try {
				String content=DeployManager.getScriptContent(name,"workflow");
				WorkflowProcess p=engine.loadProcess(content);
				ProcessInstance pi= engine.startProcess(p,handler,errorHandler);
				return pi;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//
		public void attachWorkflow(ProcessInstance instance){
			DeployManager.attachWorkflowProcessInstance(instance);
		}
		//
		public void detachWorkflow(String id){
			DeployManager.detachWorkflowProcessInstance(id);
		}
		
	}
}
