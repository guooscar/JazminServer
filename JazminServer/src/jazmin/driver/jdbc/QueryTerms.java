/**
 * 
 */
package jazmin.driver.jdbc;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yama
 *
 */
public class QueryTerms {
	static class Where{
		public String key;
		public Object value;
		public String operator;
	}
	//
	List<Where>wheres;
	List<String>orderBys;
	int limitStart=0;
	int limitEnd=-1;
	public QueryTerms() {
		wheres=new LinkedList<Where>();
		orderBys=new LinkedList<String>();
	}
	//
	public static QueryTerms create(){
		return new QueryTerms();
	}
	//
	Object [] whereValues(){
		Object []ret=new Object[wheres.size()];
		int idx=0;
		for(Where w:wheres){
			ret[idx++]=w.value;
		}
		return ret;
	}
	//
	public QueryTerms where(String key,Object value){
		return this.where(key, "=", value);
	}
	public QueryTerms where(String key,String op,Object value){
		Where w=new Where();
		w.key=key;
		w.operator=op;
		w.value=value;
		this.wheres.add(w);
		return this;
	}
	//
	public QueryTerms orderBy(String key){
		this.orderBys.add(key);
		return this;
	}
	//
	public QueryTerms limit(int start,int limit){
		this.limitStart=start;
		this.limitEnd=limit;
		return this;
	}
	//
	public QueryTerms limit(int end){
		this.limitStart=0;
		this.limitEnd=end;
		return this;
	}
}
