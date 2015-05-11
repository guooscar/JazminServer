/**
 * 
 */
package jazmin.server.im;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yama
 * 26 Dec, 2014
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface IMService{
	int id();
	boolean syncOnSession() default true;
	boolean restrictRequestRate() default true;
	boolean continuation() default false;
}
