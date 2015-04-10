package jazmin.misc.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 
 * @author yama
 * 5 Jan, 2015
 */
@Target(ElementType.FIELD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface JSONConfigField{
	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
	int size() default 0;
	boolean notNull() default false;
}
