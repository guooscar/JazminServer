/**
 * 
 */
package jazmin.server.msg;

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
@Target(ElementType.TYPE)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
@Inherited  
public @interface MessageService{
}
