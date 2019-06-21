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
	public @interface OrGroup {
		  String group() default "";
		  String childAndGroup() default "";
	}
	//
	/** 操作符 非字符串默认是= 字符串默认是like */
	public String operator() default "";

	/** 自定义查询sql */
	public String whereSql() default "";//usage: (name like #{nameOrUserName} or userName like #{nameOrUserName})

	/** 不作为查询条件 */
	public boolean ingore() default false;

	/**和表结构映射的字段名 默认就是自己;如果有多个字段，用,分隔，通过or组合起来*/
	public String field() default "";
	
	/**别的表的关联字段  必须填对应的外键字段 可以有多个按照顺序依次  逗号分隔*/
	String foreignKeyFields() default "";
	
	/**orAnd的分组,逗号分隔 eg:or name=#{name} or a.type=#type*/
	public OrGroup orGroup() default @OrGroup;
}
