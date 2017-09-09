/**
 * 
 */
package jazmin.deploy.workflow;

import jazmin.deploy.workflow.definition.Node;
import jazmin.deploy.workflow.execute.ProcessInstance;

/**
 * @author yama
 *
 */
public class WorkflowEvent {
	public static final String TYPE_ENTER="enter";
	public static final String TYPE_LEAVE="leave";
	//
	public String type;
	//
	public ProcessInstance instance;
	public Node node;
}
