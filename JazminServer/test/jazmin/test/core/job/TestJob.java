/**
 * 
 */
package jazmin.test.core.job;

import jazmin.core.Jazmin;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TestJob {
	public static void main(String[] args) {
		Jazmin.jobStore.registerJob(new SimpleJob());
		Jazmin.start();
	}
}
