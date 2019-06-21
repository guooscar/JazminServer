package jazmin.driver.jdbc.smartjdbc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jazmin.driver.jdbc.smartjdbc.annotations.QueryField.OrGroup;
import jazmin.util.StringUtil;

/**
 * 
 * @author skydu
 *
 */
public class QueryWhere {
	//
	public static class WhereStatment{
		public String sql;
		public Object[] values;
	}
	public static class Where{
		public String alias;
		public String key;
		public Object value;
		public String operator;
		public OrGroup orGroup;
		public String sql;
		public LinkedList<Object> sqlValues;
		public Where() {
			sqlValues=new LinkedList<Object>();
		}
	}
	//
	public static class OrGroupWheres{
		public String group;
		public List<Where> orWheres;
		public Map<String,List<Where>> childAndWheres;
		//
		public OrGroupWheres() {
			orWheres=new ArrayList<>();
			childAndWheres=new LinkedHashMap<>();
		}
	}
	//
	protected List<Where> wheres;
	protected Map<String,OrGroupWheres>orWheres;
	protected Set<String> orderBys;
	protected int limitStart=0;
	protected int limitEnd=-1;
	protected boolean forUpdate;
	//
	protected QueryWhere() {
		wheres=new LinkedList<>();
		orWheres=new LinkedHashMap<>();
		orderBys=new LinkedHashSet<>();
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
		return where(alias, key, op, value, null);
	}
	//
	public QueryWhere where(String alias,String key,String op,Object value,OrGroup orGroup){
		Where w=new Where();
		w.alias=alias;
		w.key=key;
		w.operator=op;
		w.value=value;
		w.orGroup=orGroup;
		addWhere(w, orGroup);
		return this;
	}
	//
	private void addWhere(Where w,OrGroup orGroup) {
		if(orGroup!=null) {
			OrGroupWheres orGroupWheres=orWheres.get(orGroup.group());
			if(orGroupWheres==null){
				orGroupWheres=new OrGroupWheres();
				orWheres.put(orGroup.group(), orGroupWheres);
			}
			if(!StringUtil.isEmpty(orGroup.childAndGroup())) {//child and
				List<Where> wheres=orGroupWheres.childAndWheres.get(orGroup.childAndGroup());
				if(wheres==null) {
					wheres=new ArrayList<>();
					orGroupWheres.childAndWheres.put(orGroup.childAndGroup(),wheres);
				}
				wheres.add(w);
			}else {//or
				orGroupWheres.orWheres.add(w);
			}
			
		}else {
			this.wheres.add(w);
		}
	}
	//
	public  QueryWhere in(String alias,String key,Object[] values) {
		return in(alias, key, values, null);
	}
	//
	public  QueryWhere in(String alias,String key,Object[] values,OrGroup orGroup) {
		if(values!=null&&values.length>0) {
			this.where(alias, key, "in", values,orGroup);
		}
		return this;
	}
	//
	public  QueryWhere notin(String alias,String key,Object[] values) {
		return notin(alias, key, values, null);
	}
	//
	public  QueryWhere notin(String alias,String key,Object[] values,OrGroup orGroup) {
		if(values!=null&&values.length>0) {
			this.where(alias, key, "not in", values,orGroup);
		}
		return this;
	}
	//
	public QueryWhere whereSql(String sql,Object ...values){
		return whereSql(sql, null, values);
	}
	//
	public QueryWhere whereSql(String sql,OrGroup orGroup,Object ...values){
		Where w=new Where();
		w.sql=sql;
		for(int i=0;i<values.length;i++){
			w.sqlValues.add(values[i]);
		}
		addWhere(w, orGroup);
		return this;
	}
	//
	public QueryWhere orderBy(String orderBy){
		this.orderBys.add(orderBy);
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
	public QueryWhere forUpdate() {
		forUpdate=true;
		return this;
	}
	//
	public WhereStatment whereStatement(){
		WhereStatment statment=new WhereStatment();
		List<Object>values=new LinkedList<Object>();
		StringBuilder sql=new StringBuilder();
		sql.append(" ");
		if(wheres.size()>0) {
			sql.append(" and ");
			appendWhereSql(values,sql,wheres, true);
		}
		for(OrGroupWheres wheres:orWheres.values()){
			if(wheres==null) {
				continue;
			}
			sql.append(" and (");
			appendWhereSql(values,sql,wheres.orWheres, false);
			boolean needAddOr=wheres.orWheres.size()>0;
			for (List<Where> andWheres : wheres.childAndWheres.values()) {
				if(andWheres.isEmpty()) {
					continue;
				}
				if(needAddOr) {
					sql.append(" or ");
				}else{
					needAddOr=true;
				}
				sql.append(" ( ");
				appendWhereSql(values,sql, andWheres, true);
				sql.append(" ) ");
			}
			sql.append(" ) ");
		}
		sql.append(" ");
		if(forUpdate) {
			sql.append(" for update ");
		}
		statment.sql=sql.toString();
		statment.values=values.toArray();
		return statment;
	}
	//
	public Object[] whereValues() {
		return whereStatement().values;
	}
	
	//
	private void appendWhereSql(List<Object> valueList,StringBuilder sql,List<Where> wheres,boolean isAnd) {
		int index=0;
		for (Where w : wheres) {
			if(index>0) {
				if(isAnd) {
					sql.append(" and ");
				}else {
					sql.append(" or ");
				}
			}
			if(w.key!=null){
				Set<String> keys=new LinkedHashSet<>();
				if(w.key.indexOf(",")!=-1) {
					String[] keyList=w.key.split(",");
					for (String key : keyList) {
						if(StringUtil.isEmpty(key.trim())) {
							continue;
						}
						keys.add(key);
					}
				}else {
					keys.add(w.key);
				}
				sql.append(" ( ");
				int keyIndex=1;
				for (String key : keys) {
					String value="?";
					if(w.alias!=null) {
						sql.append(w.alias).append(".");
					}
					sql.append("`").append(key).append("` ");
					sql.append(w.operator).append(" ");
					if(w.operator.trim().equalsIgnoreCase("like")){
						sql.append(" concat('%',"+value+",'%') ");
						valueList.add(w.value);
					}else if(w.operator.trim().equalsIgnoreCase("in")) {
						Object[] values=(Object[])w.value;
						if(values!=null&&values.length>0) {
							sql.append(" ( ");
							for (int i = 0; i < values.length; i++) {
								sql.append(" ?,");
								valueList.add(values[i]);
							}
							sql.deleteCharAt(sql.length() - 1);
							sql.append(" ) ");
						}
					}else if(w.operator.trim().equalsIgnoreCase("not in")) {
						Object[] values=(Object[])w.value;
						if(values!=null&&values.length>0) {
							sql.append(" ( ");
							for (int i = 0; i < values.length; i++) {
								sql.append(" ?,");
								valueList.add(values[i]);
							}
							sql.deleteCharAt(sql.length() - 1);
							sql.append(" ) ");
						}
					}else{
						sql.append("  "+value+" ");
						valueList.add(w.value);
					}
					if(keyIndex<keys.size()) {
						sql.append(" or ");
					}
					keyIndex++;
				}
				sql.append(" ) ");
			}else{
				sql.append(" "+ w.sql+" ");
				if(w.sqlValues!=null&&w.sqlValues.size()>0) {
					valueList.addAll(w.sqlValues);
				}
			}
			index++;
		}
	}
	//
	/**
	 * @return the orderBy
	 */
	public Set<String> getOrderBys() {
		return orderBys;
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
