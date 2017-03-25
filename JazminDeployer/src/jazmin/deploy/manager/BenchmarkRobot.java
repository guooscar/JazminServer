/**
 * 
 */
package jazmin.deploy.manager;

/**
 * @author yama
 *
 */
public interface BenchmarkRobot {
	void start()throws Exception;
	void loop()throws Exception;
	void end()throws Exception;
	String name();
}
