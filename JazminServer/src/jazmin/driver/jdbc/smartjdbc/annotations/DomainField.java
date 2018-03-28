package jazmin.driver.jdbc.smartjdbc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
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
@Inherited  
public @interface DomainField {
	
	/**这个字段是别的表的关联字段  必须填对应的外键字段 可以有多个按照顺序依次y以逗号分隔*/
	String foreignKeyFields() default "";
	
	/**真实字段 eg:userName 注意:不是user_name*/
	String field() default "";

	/**select distinct field*/
	boolean distinct() default false;

	/**select max() or sum() avg() */
	String statFunc() default "";
	/**ignore or not when select*/
	boolean ignoreWhenSelect() default false;
}