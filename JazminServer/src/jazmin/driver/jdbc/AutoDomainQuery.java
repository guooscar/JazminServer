package jazmin.driver.jdbc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author skydu
 *
 */
public class AutoDomainQuery{

	public static class AutoDomainOrderBy{
		//
		public static final int SORT_ASC=1;
		public static final int SORT_DESC=2;
		//
		public AutoDomainOrderBy() {
			
		}
		public AutoDomainOrderBy(String fieldName,int sort) {
			this.fieldName=fieldName;
			this.sort=sort;
		}
		//
		public String fieldName;//userName
		public int sort;
	}
	
	public Class<?> domainClass;
	
	public Map<String,Object> queryParams;
	
	public Map<String,String> operators;
	
	public int pageIndex=0;
	
	public int pageSize=20;
	
	public List<AutoDomainOrderBy> autoDomainOrderBies;
	//
	public AutoDomainQuery() {
		queryParams=new LinkedHashMap<>();
		operators=new LinkedHashMap<>();
		autoDomainOrderBies=new ArrayList<>();
	}
}
