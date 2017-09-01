/**
 * 
 */
package jazmin.deploy.manager;

import java.io.IOException;

import jazmin.deploy.workflow.WorkflowEngine;
import jazmin.deploy.workflow.definition.WorkflowProcess;
import jazmin.deploy.workflow.execute.ExecuteHandler;
import jazmin.deploy.workflow.execute.ProcessInstance;

/**
 * @author yama
 *
 */
public interface RobotWorkflowEngineContext {
	public ProcessInstance startWorkflow(String name,ExecuteHandler handler);
	//
	public  class RobotWorkflowEngineContextImpl implements RobotWorkflowEngineContext{
		WorkflowEngine engine;
		public RobotWorkflowEngineContextImpl(WorkflowEngine engine) {
			this.engine=engine;
		}
		//
		public ProcessInstance startWorkflow(String name,ExecuteHandler handler){
			try {
				String content=DeployManager.getWorkflowScriptContent(name);
				WorkflowProcess p=engine.loadProcess(content);
				return engine.startProcess(p,handler);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
