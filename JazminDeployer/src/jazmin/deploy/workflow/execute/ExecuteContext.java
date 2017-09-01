/**
 * 
 */
package jazmin.deploy.workflow.execute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.deploy.workflow.definition.Node;

/**
 * @author yama
 *
 */
public class ExecuteContext {
	Node currentNode;
	Map<String,Object>variables;
	ProcessInstance instance;
	public ExecuteContext(ProcessInstance instance) {
		variables=new ConcurrentHashMap<>();
		this.instance=instance;
	}
	//
	public void setVariable(String key,Object value){
		variables.put(key, value);
	}
	//
	public Object getVariable(String key){
		return variables.get(key);
	}
	//
	public Node getNode(){
		return currentNode;
	}
	//
	public void transtion(String toNodeId){
		instance.transition(currentNode, toNodeId);
	}
}
