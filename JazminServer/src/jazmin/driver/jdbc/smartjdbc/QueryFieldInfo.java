package jazmin.driver.jdbc.smartjdbc;

import java.lang.reflect.Field;

import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryField.OrGroup;

/**
 * 
 * @author skydu
 *
 */
public class QueryFieldInfo {
	//
	public QueryField queryField;
	public Field field;
	public Class<?> fieldType;
	public Object value;
	public OrGroup orGroup;
}
