/**
 * 
 */
package jazmin.deploy.workflow.test;

import jazmin.deploy.workflow.execute.Execute;
import jazmin.deploy.workflow.execute.ExecuteContext;

/**
 * @author yama
 *
 */
public class AddIntegerExecute implements Execute{
	//
	@Override
	public void execute(ExecuteContext ctx) throws Exception {
		Integer vv=(Integer) ctx.getVariable("add-int");
		if(vv==null){
			vv=0;
			ctx.setVariable("add-int", vv);
		}
		vv++;
		ctx.setVariable("add-int", vv);
		System.err.println("AddIntegerExecute:"+vv);
	}
}
