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
	public int pageIndex;
	
	@QueryField(ingore=true)
	public int pageSize;
	
	@QueryField(ingore=true)
	public Integer orderType;
	
	@QueryField(ingore=true)
	public String[] sortFields;//sort fileds order
	//
	public static Integer defaultOrderType=null;
	public static Integer defaultPageSize=20;
	//
	public Query(){
		pageSize=20;//
		pageIndex=1;//从1开始
		orderType=defaultOrderType;
		if(defaultPageSize!=null) {
			pageSize=defaultPageSize;
		}
	}
	//
	public int getStartPageIndex(){
		return (pageIndex-1)*pageSize;
	}
}
