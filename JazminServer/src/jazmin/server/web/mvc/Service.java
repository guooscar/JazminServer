/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 
 * @author yama
 * 29 Dec, 2014
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface Service{
	String id();
	HttpMethod method() default HttpMethod.GET;
	boolean index() default false;
	int queryCount() default -1;
	boolean syncOnSession() default false;
}