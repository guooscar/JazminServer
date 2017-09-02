/**
 * 
 */
package jazmin.deploy.controller;

import java.io.IOException;

import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.workflow.definition.WorkflowProcess;
import jazmin.deploy.workflow.execute.ProcessInstance;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;

/**
 * @author yama 6 Jan, 2015
 */
@Controller(id = "workflow")
public class WorkflowController extends AuthBaseController{
	//
	@Service(id = "editor")
	public void editor(Context ctx) {
		ctx.put("tasks", DeployManager.workflowEngine.getTaskTemplates());
		ctx.view(new ResourceView("/jsp/workflow.jsp"));
	}

	//
	@Service(id = "get_workflow_list", method = HttpMethod.POST)
	public void getWorkflowList(Context ctx) {
		ctx.put("list", DeployManager.getWorkflowScripts());
		ctx.view(new JsonView());
	}

	//
	@Service(id = "get_workflow_task_templates", method = HttpMethod.POST)
	public void getWorkflowTaskTemplates(Context ctx) {
		ctx.put("list", DeployManager.workflowEngine.getTaskTemplates());
		ctx.view(new JsonView());
	}

	//
	@Service(id = "get_workflow_content", method = HttpMethod.POST)
	public void getWorkflowContent(Context ctx) throws IOException {
		String source = DeployManager.getWorkflowScriptContent(ctx.getString("name", true));
		ctx.put("content", source);
		ctx.view(new JsonView());
	}

	//
	@Service(id = "save_workflow_content", method = HttpMethod.POST)
	public void saveWorkflowContent(Context ctx) throws IOException {
		String name = ctx.getString("name", true);
		String content = ctx.getString("content", true);
		DeployManager.saveWorkflowScript(name, content);
		ctx.view(new JsonView());
	}

	//
	@Service(id = "delete_workflow_content", method = HttpMethod.POST)
	public void deleteWorkflowContent(Context ctx) throws IOException {
		String name = ctx.getString("name", true);
		DeployManager.deleteWorkflowScript(name);
		ctx.view(new JsonView());
	}

	//
	@Service(id = "start_workflow_instance", method = HttpMethod.POST)
	public void startWorkflow(Context ctx) throws IOException {
		String name = ctx.getString("name", true);
		String script = DeployManager.getWorkflowScriptContent(name);
		WorkflowProcess process = DeployManager.workflowEngine.loadProcess(script);
		ProcessInstance instance = DeployManager.workflowEngine.startProcess(process);
		ctx.request().session().setAttribute("WorkflowInstance", instance);
		ctx.put("instance", instance);
		ctx.view(new JsonView());
	}
	//
	@Service(id = "attach_workflow_instance", method = HttpMethod.POST)
	public void attachWorkflow(Context ctx) throws IOException {
		String name = ctx.getString("id", true);
		ProcessInstance instance = DeployManager.getAttachedWorkflowProcessInstance(name);
		ctx.request().session().setAttribute("WorkflowInstance", instance);
		ctx.put("instance", instance);
		ctx.view(new JsonView());
	}
	//
	//
	@Service(id = "remove_attach_workflow_instance", method = HttpMethod.POST)
	public void removeAttachWorkflow(Context ctx) throws IOException {
		String name = ctx.getString("id", true);
		DeployManager.detachWorkflowProcessInstance(name);
		ctx.view(new JsonView());
	}
	//
	//
	@Service(id = "get_workflow_attach_list", method = HttpMethod.POST)
	public void getWorkflowAttachList(Context ctx) {
		ctx.put("list", DeployManager.getAttachedWorkflowProcessInstances());
		ctx.view(new JsonView());
	}
	//
	@Service(id = "get_workflow_instance", method = HttpMethod.POST)
	public void getWorkflowInstance(Context ctx) throws IOException {
		ProcessInstance instance = (ProcessInstance) ctx.request().session().getAttribute("WorkflowInstance");
		ctx.put("instance", instance);
		ctx.view(new JsonView());
	}

	//
	@Service(id = "signal_workflow_instance", method = HttpMethod.POST)
	public void signalWorkflowInstance(Context ctx) throws IOException {
		String id = ctx.getString("node", true);
		ProcessInstance instance = (ProcessInstance) ctx.request().session().getAttribute("WorkflowInstance");
		if (instance != null) {
			instance.signal(id);
		}
		ctx.view(new JsonView());
	}
	//

}
