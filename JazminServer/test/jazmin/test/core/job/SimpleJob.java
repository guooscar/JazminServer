/**
 * 
 */
package jazmin.test.core.job;

import jazmin.core.job.JobDefine;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class SimpleJob {
	private int count1;
	
	@JobDefine(cron="0/1 * * * * ?")
	public void job(){
		System.out.println(count1++);
	}
}
