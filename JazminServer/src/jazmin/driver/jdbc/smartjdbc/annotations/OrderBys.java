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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OrderBys {
	//
	public static @interface OrderBy {
		  public int orderType();
		  public String sql();
	}
	public OrderBy[] orderBys() default {};
}