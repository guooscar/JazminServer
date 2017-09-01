package jazmin.deploy.workflow.builtin;

import jazmin.deploy.workflow.definition.Node;
import jazmin.deploy.workflow.execute.Execute;
import jazmin.deploy.workflow.execute.ExecuteContext;

/**
 * 
 * @author yama
 *
 */
public class PrintNodeExecute implements Execute {
	//
	@Override
	public void execute(ExecuteContext ctx) throws Exception {
		Node node=ctx.getNode();
		System.out.println("Node[id:"+node.id+" name:"+node.name+" type:"+node.type+"]");
	}
}
