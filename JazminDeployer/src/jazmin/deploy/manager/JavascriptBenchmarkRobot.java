/**
 * 
 */
package jazmin.deploy.manager;

import java.util.function.IntConsumer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * @author yama
 *
 */
public class JavascriptBenchmarkRobot implements BenchmarkRobot{
	Runnable startCallback;
	Runnable endCallback;
	IntConsumer loopCallback;
	//
	public void start(Runnable startCallback){
		this.startCallback=startCallback;
	}
	//
	public void loop(IntConsumer loopCallback){
		this.loopCallback=loopCallback;
	}
	//
	public void end(Runnable endCallback){
		this.endCallback=endCallback;
	}
	//
	public void log(String log){
		session.log(log);
	}
	//
	public void sleep(int milSeconds){
		try {
			Thread.sleep(milSeconds);
		} catch (InterruptedException e) {
			log(e.getMessage());
		}
	}
	//
	//-------------------------------------------------------------------------------------
	//
	String name;
	String jsContent;
	BenchmarkSession session;
	//
	public JavascriptBenchmarkRobot(BenchmarkSession session,String name,String jsContent)
	throws ScriptException{
		this.name=name;
		this.jsContent=jsContent;
		this.session=session;
		loadJavaScript(jsContent);
	}
	//
	private void loadJavaScript(String source)throws ScriptException{
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		SimpleScriptContext ssc=new SimpleScriptContext();
		ssc.setAttribute("benchmark",this, ScriptContext.ENGINE_SCOPE);
		ssc.setAttribute("http",new BenchmarkHttp(session), ScriptContext.ENGINE_SCOPE);
		String commonScript=
					"load('nashorn:mozilla_compat.js');"+
					"importPackage(Packages.jazmin.deploy.manager);\n";
		engine.eval(commonScript+source, ssc); 
	}
	//
	@Override
	public String name() {
		return name;
	}
	//
	@Override
	public void start() throws Exception {
		if(startCallback!=null){
			startCallback.run();
		}
	}
	
	@Override
	public void loop(int count) throws Exception {
		if(loopCallback!=null){
			loopCallback.accept(count);
		}
	}
	@Override
	public void end() throws Exception {
		if(endCallback!=null){
			endCallback.run();
		}
	}	
}
