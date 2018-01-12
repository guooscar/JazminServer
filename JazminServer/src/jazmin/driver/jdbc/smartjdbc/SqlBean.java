package jazmin.driver.jdbc.smartjdbc;

/**
 * 
 * @author skydu
 *
 */
public class SqlBean {

	public String sql;
	
	public Object[] parameters;
	//
	public SqlBean(String sql, Object[] parameters) {
		this.sql=sql;
		this.parameters=parameters;
	}
	//
	public SqlBean(String sql) {
		this(sql,new Object[0]);
	}
}
