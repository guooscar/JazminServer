package jazmin.driver.jdbc.smartjdbc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author skydu
 *
 */
@Target(ElementType.FIELD)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
public @interface InnerJoin {
	//
	public String table1Field() default "id";

	public String table2Field() default "id";
	
	public String[] table1Fields() default {};

	public String[] table2Fields() default {};

	public Class<?> table2() default void.class;
}