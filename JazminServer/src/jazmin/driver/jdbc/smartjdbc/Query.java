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
	public int pageSize=20;
	
	@QueryField(ingore=true)
	public Integer orderType;
	
	@QueryField(ingore=true)
	public String[] sortFields;//sort fileds order
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
