package jazmin.driver.jdbc.smartjdbc;

import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;

/**
 * 
 * @author icecooly
 *
 */
public class Query {
	//
	@QueryField(ingore=true)
	public int pageIndex;//从1开始
	
	@QueryField(ingore=true)
	public int pageSize=20;
	
	@QueryField(ingore=true)
	public Integer orderType;
	//
	public static Integer defaultOrderType=null;
	public static Integer defaultPageSize=20;
	//
	public Query(){
		orderType=defaultOrderType;
		pageIndex=1;
		if(defaultPageSize!=null) {
			pageSize=defaultPageSize;
		}
	}
	//
	public int getStartPageIndex(){
		return (pageIndex-1)*pageSize;
	}
}
