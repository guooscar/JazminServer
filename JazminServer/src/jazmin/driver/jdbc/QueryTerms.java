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
		//
		public String sql;
		public LinkedList<Object> sqlValues;
		public Where() {
			sqlValues=new LinkedList<Object>();
		}
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
	public QueryTerms where(String key,Object value){
		return this.where(key, "=", value);
	}
	//
	public QueryTerms whereSql(String sql,Object ...values){
		Where w=new Where();
		w.sql=sql;
		for(int i=0;i<values.length;i++){
			w.sqlValues.add(values[i]);
		}
		this.wheres.add(w);
		return this;
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
	//
	Object [] whereValues(){
		List<Object>ret=new LinkedList<Object>();
		for(Where w:wheres){
			if(w.key!=null){
				ret.add(w.value);
			}else{
				ret.addAll(w.sqlValues);
			}
		}
		return ret.toArray();
	}
	//
	public String whereStatement(){
		StringBuilder sql=new StringBuilder();
		sql.append(" ");
		for(Where w:wheres){
			if(w.key!=null){
				sql.append(" and ");
				sql.append("`").append(w.key).append("` ");
				sql.append(w.operator).append(" ");
				if(w.operator.trim().equalsIgnoreCase("like")){
					sql.append(" concat('%',?,'%') ");
				}else{
					sql.append(" ? ");
				}
			}else{
				sql.append(" "+ w.sql+" ");
			}
		}
		sql.append(" ");
		return sql.toString();
	}
}
