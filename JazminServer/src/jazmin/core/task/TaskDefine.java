/**
 * 
 */
package jazmin.core.task;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * the annotation of jazmin task
 * @author yama
 * 25 Dec, 2014
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface TaskDefine{
	/**
	 * initial delay time
	 * @return the initial delay time
	 */
	long initialDelay();
	/**
	 * the repeat time period
	 * @return repeat time period
	 */
	long period();
	/**
	 * the time unit of repeat time
	 * @return time unit of repeat time
	 */
	TimeUnit unit();
	//
	boolean runInThreadPool() default true;
}
