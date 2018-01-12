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
public @interface QueryField {
	//
	public @interface WhereSql {
		public String sql() default "";
		public String[] values() default {};
	}
	//
	/** 操作符 非字符串默认是= 字符串默认是like */
	public String operator() default "";

	/** 自定义查询sql */
	public WhereSql whereSql() default @WhereSql();

	/** 不作为查询条件 */
	public boolean ingore() default false;

	/**和表结构映射的字段名 默认就是自己*/
	public String field() default "";
}
