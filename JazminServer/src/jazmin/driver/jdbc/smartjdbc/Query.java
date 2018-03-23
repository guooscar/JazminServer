package jazmin.driver.jdbc.smartjdbc;

import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;

/**
 * 
 * @author icecooly
 *
 */
public class Query {
	//
	public static final int SORT_TYPE_ASC=1;
	public static final int SORT_TYPE_DESC=2;
	//
	@QueryField(ingore=true)
	public int pageIndex;//从1开始
	
	@QueryField(ingore=true)
	public int pageSize;
	
	@QueryField(ingore=true)
	public Integer orderType;
	//
	public Query(){
	}
	//
	public int getStartPageIndex(){
		return (pageIndex-1)*pageSize;
	}
}
