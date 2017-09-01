/**
 * 
 */
package jazmin.deploy.manager;

/**
 * @author yama
 *
 */
public interface BenchmarkRobot {
	void start(int count)throws Exception;
	void loop(int count)throws Exception;
	void end()throws Exception;
	String name();
}
