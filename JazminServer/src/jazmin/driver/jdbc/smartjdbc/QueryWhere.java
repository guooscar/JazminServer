package jazmin.driver.jdbc.smartjdbc;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author skydu
 *
 */
public class QueryWhere {
	//
	public static class Where{
		public String alias;
		public String key;
		public Object value;
		public String operator;
		public String sql;
		public LinkedList<Object> sqlValues;
		public Where() {
			sqlValues=new LinkedList<Object>();
		}
	}
	//
	protected List<Where> wheres;
	protected String orderBy;
	protected int limitStart=0;
	protected int limitEnd=-1;
	//
	private QueryWhere() {
		wheres=new LinkedList<Where>();
	}
	//
	public static QueryWhere create(){
		return new QueryWhere();
	}
	//
	public QueryWhere where(String key,Object value){
		return this.where(key, "=", value);
	}
	//
	public QueryWhere where(String key,String op,Object value){
		this.where(null, key, op, value);
		return this;
	}
	//
	public QueryWhere where(String alias,String key,String op,Object value){
		Where w=new Where();
		w.alias=alias;
		w.key=key;
		w.operator=op;
		w.value=value;
		this.wheres.add(w);
		return this;
	}
	//
	public  QueryWhere in(String alias,String key,Object[] values) {
		this.where(alias, key, "in", values);
		return this;
	}
	//
	public QueryWhere whereSql(String sql,Object ...values){
		Where w=new Where();
		w.sql=sql;
		for(int i=0;i<values.length;i++){
			w.sqlValues.add(values[i]);
		}
		this.wheres.add(w);
		return this;
	}
	//
	public QueryWhere orderBy(String orderBy){
		this.orderBy=orderBy;
		return this;
	}
	//
	public QueryWhere limit(int start,int limit){
		this.limitStart=start;
		this.limitEnd=limit;
		return this;
	}
	//
	public QueryWhere limit(int end){
		this.limitStart=0;
		this.limitEnd=end;
		return this;
	}
	//
	public Object[] whereValues(){
		List<Object>ret=new LinkedList<Object>();
		for(Where w:wheres){
			if(w.key!=null){
				if(w.operator.equals("in")) {
					Object[] values=(Object[])w.value;
					if(values!=null&&values.length>0) {
						for (Object value : values) {
							ret.add(value);
						}
					}
				}else {
					ret.add(w.value);
				}
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
				if(w.alias!=null) {
					sql.append(w.alias).append(".");
				}
				sql.append("`").append(w.key).append("` ");
				sql.append(w.operator).append(" ");
				if(w.operator.trim().equalsIgnoreCase("like")){
					sql.append(" concat('%',?,'%') ");
				}else if(w.operator.trim().equalsIgnoreCase("in")) {
					Object[] values=(Object[])w.value;
					if(values!=null&&values.length>0) {
						sql.append(" ( ");
						for (int i = 0; i < values.length; i++) {
							sql.append(" ?,");
						}
						sql.deleteCharAt(sql.length() - 1);
						sql.append(" ) ");
					}
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
	//
	/**
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}
	/**
	 * @return the limitStart
	 */
	public int getLimitStart() {
		return limitStart;
	}
	/**
	 * @return the limitEnd
	 */
	public int getLimitEnd() {
		return limitEnd;
	}
	
	public List<Where> getWheres() {
		return wheres;
	}
	
}
