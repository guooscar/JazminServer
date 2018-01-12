package jazmin.driver.jdbc.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * 表示了domain类的 关联关系
 * @author yama
 * 27 Dec, 2014
 */
@Target(ElementType.TYPE)  
@Repeatable(ForeignKeys.class)
@Inherited  
public @interface ForeignKey {
	Class<?> table();
	String prefix();
	String on() default "";
}
 