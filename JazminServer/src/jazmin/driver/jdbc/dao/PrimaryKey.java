package jazmin.driver.jdbc.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * 默认使用id 作为主键，如果domain类中没有名字为id的字段，则需要使用PrimaryKey注解
 * 指定一个
 * @author yama
 * 27 Dec, 2014
 */
@Target(ElementType.FIELD)  
@Inherited  
public @interface PrimaryKey {
	String value() default "";
	boolean autoIncr() default true;
}
