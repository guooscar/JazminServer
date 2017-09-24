/**
 * 
 */
package jazmin.core.notification;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yama
 *
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface NotificationDefine{
	/**
	 * name of notification 
	 */
	String event();
	/**
	 * is async run
	 */
	boolean async() default true;
	
}
