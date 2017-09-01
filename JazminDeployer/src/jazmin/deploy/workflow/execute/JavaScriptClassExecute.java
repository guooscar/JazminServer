/**
 * 
 */
package jazmin.deploy.workflow.execute;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import jazmin.deploy.workflow.definition.Node;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class JavaScriptClassExecute implements Execute,ScriptExecuteContext{
	//
	private static Logger logger=LoggerFactory.get(JavaScriptClassExecute.class);
	private String script;
	public JavaScriptClassExecute(String script) {
		this.script=script;
	}
	ExecuteContext ctx;
	//
	@Override
	public void execute(ExecuteContext ctx) throws Exception {
		this.ctx=ctx;
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		Bindings bindings=engine.createBindings();
		SimpleScriptContext ssc=new SimpleScriptContext();
		ssc.setAttribute("$",this, ScriptContext.ENGINE_SCOPE);
		bindings.put("$", this);
		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		String commonScript=
					"load('nashorn:mozilla_compat.js');"+
					"importPackage(Packages.io.itit.workflow);\n"+
					"importPackage(Packages.io.itit.definition);\n"+
					"importPackage(Packages.io.itit.execute);\n";
		engine.eval(commonScript+script, ssc); 
	}
	//---------------------------------------------------------------------------------
	//
	@Override
	public void log(String message) {
		logger.info(message);
	}
	@Override
	public void halt(String message) {
		throw new RuntimeException(message);
	}
	@Override
	public Object getVariable(String key) {
		return ctx.getVariable(key);
	}
	@Override
	public void setVariable(String key, Object value) {
		ctx.setVariable(key, value);
	}
	@Override
	public Node getNode() {
		return ctx.getNode();
	}
	@Override
	public void transtion(String toNodeId) {
		ctx.transtion(toNodeId);
	}
}
