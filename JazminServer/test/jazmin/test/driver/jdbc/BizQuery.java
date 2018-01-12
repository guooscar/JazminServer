package jazmin.test.driver.jdbc;

import java.util.Date;

import jazmin.driver.jdbc.smartjdbc.Query;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;

/**
 * 
 * @author skydu
 *
 */
public class BizQuery extends Query{
	//
	public static final int ORDER_TYPE_CREATE_TIME_DESC=100;
	public static final int ORDER_TYPE_CREATE_TIME_ASC=101;
	//
	@QueryField(operator=">=",field="createTime")
	public Date startCreateTime;
	@QueryField(operator="<",field="createTime")
	public Date endCreateTime;
	@QueryField(operator=">=",field="updateTime")
	public Date startUpdateTime;
	@QueryField(operator="<",field="updateTime")
	public Date endUpdateTime;
}
