/**
 * 
 */
package jazmin.deploy.workflow.execute;

import jazmin.deploy.workflow.definition.Node;

/**
 * @author yama
 *
 */
public interface ScriptExecuteContext {
	void log(String message);
	void halt(String message);
	//
	Object getVariable(String key);
	void setVariable(String key,Object value);
	//
	Node getNode();
	void transtion(String toNodeId);
}
