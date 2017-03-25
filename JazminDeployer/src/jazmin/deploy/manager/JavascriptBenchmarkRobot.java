/**
 * 
 */
package jazmin.deploy.manager;

/**
 * @author yama
 *
 */
public class JavascriptBenchmarkRobot implements BenchmarkRobot{
	Runnable startCallback;
	Runnable endCallback;
	Runnable loopCallback;
	//
	public void start(Runnable startCallback){
		this.startCallback=startCallback;
	}
	//
	public void loop(Runnable loopCallback){
		this.loopCallback=loopCallback;
	}
	//
	public void end(Runnable endCallback){
		this.endCallback=endCallback;
	}
	//
	@Override
	public String name() {
		return null;
	}
	//
	@Override
	public void start() throws Exception {
		if(startCallback!=null){
			startCallback.run();
		}
	}
	@Override
	public void loop() throws Exception {
		if(loopCallback!=null){
			loopCallback.run();
		}
	}
	@Override
	public void end() throws Exception {
		if(endCallback!=null){
			endCallback.run();
		}
	}	
}
