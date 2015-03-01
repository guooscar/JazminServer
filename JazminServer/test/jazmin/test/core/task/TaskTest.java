/**
 * 
 */
package jazmin.test.core.task;

import jazmin.core.Jazmin;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TaskTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Jazmin.taskStore.registerTask(new SimpleTask());
		Jazmin.start();
	}
}
