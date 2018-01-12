package jazmin.test.driver.jdbc;

import java.util.List;

import jazmin.driver.jdbc.smartjdbc.Query;
import jazmin.driver.jdbc.smartjdbc.SmartDAO;

/**
 * 
 * @author skydu
 *
 */
public class DemoDAO extends SmartDAO{

	/**
	 * 
	 */
	@Override
	public <T> List<T> getList(Query query){
		Class<T> domainClass=getDomainClass(query);
		return getList(new BizSelectProvider(domainClass).query(query));
	}
	
}
