/**
 * 
 */
package jazmin.core.job;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation of jazmin job
 * @author yama
 * 25 Dec, 2014
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface JobDefine{
	/**
	 * the cron express of this job
	 * @return the cron express
	 */
	String cron();
}
