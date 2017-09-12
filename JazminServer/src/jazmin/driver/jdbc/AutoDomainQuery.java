package jazmin.driver.jdbc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	
	public static class AutoDomainWhere{
		public String fieldName;
		public Object value;
		public String operator;
		//
		public AutoDomainWhere() {
			
		}
		public AutoDomainWhere(String fieldName,Object value) {
			this(fieldName, "=", value);
		}
		public AutoDomainWhere(String fieldName,String operator,Object value) {
			this.fieldName=fieldName;
			this.operator=operator;
			this.value=value;
		}
	}
	
	public Class<?> domainClass;
	
	public List<AutoDomainWhere> queryParams;
	
	public int pageIndex=0;
	
	public int pageSize=20;
	
	public List<AutoDomainOrderBy> autoDomainOrderBies;
	//
	public AutoDomainQuery() {
		queryParams=new LinkedList<>();
		autoDomainOrderBies=new ArrayList<>();
	}
}
