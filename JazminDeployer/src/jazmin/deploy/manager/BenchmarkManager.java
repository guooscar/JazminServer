/**
 * 
 */
package jazmin.deploy.manager;


import java.io.File;
import java.nio.file.Files;

import javax.script.ScriptException;

import jazmin.deploy.manager.BenchmarkSession.RobotFactory;


/**
 * @author yama
 *
 */
public class BenchmarkManager {
	//
	public BenchmarkSession startSession()throws Exception{
		String file=new String(Files.readAllBytes(new File("workspace/benchmark/test_benchmark.js").toPath()));
		BenchmarkSession session=new BenchmarkSession();
		};
		session.start("jsrobot",rf, 10, 10, 0);
		return session;
	}
	//
	public static void main(String[] args) throws Exception{
		BenchmarkManager manager=new BenchmarkManager();
		manager.startSession();
	}
}
