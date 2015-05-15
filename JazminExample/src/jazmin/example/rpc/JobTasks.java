/**
 * 
 */
package jazmin.example.rpc;

import java.util.concurrent.TimeUnit;

import jazmin.core.job.JobDefine;
import jazmin.core.task.TaskDefine;

/**
 * @author yama
 *
 */
public class JobTasks {
	@JobDefine(cron="0 * * * * ?")
	public void testJob(){
		System.out.println("test job");
	}
	//
	@TaskDefine(initialDelay=10,period=10,unit=TimeUnit.SECONDS)
	public void testTask(){
		System.out.println("test task");
	}
}
