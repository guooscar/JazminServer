/**
 * 
 */
package jazmin.test.core.task;

import java.util.concurrent.TimeUnit;

import jazmin.core.task.TaskDefine;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class SimpleTask {
	private int count1;
	private int count2;
	
	@TaskDefine(initialDelay=5,period=5,unit=TimeUnit.SECONDS)
	public void task1(){
		System.out.println(count1++);
	}
	
	@TaskDefine(initialDelay=3,period=3,unit=TimeUnit.SECONDS)
	public void task2(){
		System.out.println(count2++);
	}
}
