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
public class PrintExecute implements Execute{
	//
	@Override
	public void execute(ExecuteContext ctx) throws Exception {
		String str=(String) ctx.getVariable("print-content");
		System.err.println("PrintExecute:"+str);
	}
}
