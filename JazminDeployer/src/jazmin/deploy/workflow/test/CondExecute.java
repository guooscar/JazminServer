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
public class CondExecute implements Execute{
	//
	@Override
	public void execute(ExecuteContext ctx) throws Exception {
		Integer vv=(Integer) ctx.getVariable("add-int");
		if(vv==null){
			vv=0;
		}
		System.err.println("CondExecute:"+vv);
		if(vv>5){
			ctx.transition("print");
		}else{
			ctx.transition("add");
		}
		
	}
}
