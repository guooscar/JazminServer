package jazmin.test.driver.jdbc;

import jazmin.driver.jdbc.smartjdbc.Query;
import jazmin.driver.jdbc.smartjdbc.provider.SelectProvider;

/**
 * 
 * @author skydu
 *
 */
public class BizSelectProvider extends SelectProvider{
	//
	public BizSelectProvider(Class<?> domainClass) {
		super(domainClass);
	}
	//
	@Override
	protected void addOrderBy(Query query) {
		super.addOrderBy(query);
		if(query.orderType==BizQuery.ORDER_TYPE_CREATE_TIME_ASC) {
			orderBy(" create_time asc");
		}
		if(query.orderType==BizQuery.ORDER_TYPE_CREATE_TIME_DESC) {
			orderBy(" create_time desc");
		}
	}

}
