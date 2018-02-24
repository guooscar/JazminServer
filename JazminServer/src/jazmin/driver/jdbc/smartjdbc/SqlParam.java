package jazmin.driver.jdbc.smartjdbc;

/**
 * 
 * @author skydu
 *
 */
public class SqlParam {

	public String name;
	
	public Object value;
	
	public SqlParam(String name,Object value) {
		this.name=name;
		this.value=value;
	}
}
