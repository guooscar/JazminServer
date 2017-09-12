package jazmin.driver.jdbc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.AutoDomainQuery.AutoDomainOrderBy;
import jazmin.util.IOUtil;
import jazmin.util.JSONUtil;

/**
 * 
 * @author skydu
 *
 */
public class AutoDomainDAO extends JazminDAO{

	private String tableNamePrefix="t";
	//
	protected  String convertFieldName(String name) {
		StringBuffer result = new StringBuffer();
		for (char c : name.toCharArray()) {
			if (Character.isUpperCase(c)) {
				result.append("_");
			}
			result.append(Character.toLowerCase(c));
		}
		return result.toString();
	}
	
	protected String getTableName(Class<?> type) {
		String tableName=tableNamePrefix+convertFieldName(type.getSimpleName());
		return tableName;
	}
	
	private void checkExcludeProperties(String []excludeProperties,Class<?>type){
		for(String p:excludeProperties){
			try {
				if(type.getField(p)==null){	
					return;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("can not find property:"+
						p+" in type:"+type.getName());
			} 
		}
	}
	
	private String querySql(Class<?>type,QueryTerms qt,String... excludeProperties){
		StringBuilder sql=new StringBuilder();
		checkExcludeProperties(excludeProperties,type);
		String tableName=getTableName(type);
		sql.append("select ");
		if(excludeProperties==null||excludeProperties.length==0){
			sql.append(" * ");
		}else{
			Set<String> excludesNames = new TreeSet<String>();
			for (String e : excludeProperties) {
				excludesNames.add(e);
			}
			for (Field f : type.getFields()) {
				if (excludesNames.contains(f.getName())) {
					continue;
				}
				String fieldName = convertFieldName(f.getName());
				if (Modifier.isStatic(f.getModifiers())) {
					continue;
				}
				sql.append("`").append(fieldName).append("`").append(",");
			}
			sql.deleteCharAt(sql.length()-1);
		}
		sql.append(" from ").append(tableName);
		sql.append(" where 1=1");
		sql.append(qt.whereStatement());
		if(!qt.orderBys.isEmpty()){
			sql.append(" order by ");
			for(String k:qt.orderBys){
				sql.append(k).append(",");
			}
			sql.deleteCharAt(sql.length()-1);
		}
		if(qt.limitEnd!=-1){
			sql.append(" limit ").
			append(qt.limitStart).
			append(",").
			append(qt.limitEnd);
		}
		return sql.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected <T>T convertBean(Class<?>type,ResultSet rs){
		try{
			Object instance=type.newInstance();
			convertBean(instance,rs);
			return (T) instance;
		}catch(Exception e){
			throw new ConnectionException(e);
		}
	} 
	//
	/**
	 * get data from result set and convert to bean 
	 */
	protected void convertBean(Object o, ResultSet rs, String... excludeProperties)
			throws Exception {
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeProperties) {
			excludesNames.add(e);
		}
		Class<?> type = o.getClass();
		checkExcludeProperties(excludeProperties,type);
		for (Field f : type.getFields()) {
			if (excludesNames.contains(f.getName())) {
				continue;
			}
			String fieldName = convertFieldName(f.getName());
			Class<?> fieldType = f.getType();
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			Object value = null;
			if (fieldType.equals(String.class)) {
				value = rs.getString(fieldName);
			} else if (fieldType.equals(Integer.class)
					|| fieldType.equals(int.class)) {
				value = rs.getInt(fieldName);
			} else if (fieldType.equals(Short.class)
					|| fieldType.equals(short.class)) {
				value = rs.getShort(fieldName);
			} else if (fieldType.equals(Long.class)
					|| fieldType.equals(long.class)) {
				value = rs.getLong(fieldName);
			} else if (fieldType.equals(Double.class)
					|| fieldType.equals(double.class)) {
				value = rs.getDouble(fieldName);
			} else if (fieldType.equals(Float.class)
					|| fieldType.equals(float.class)) {
				value = rs.getFloat(fieldName);
			} else if (fieldType.equals(Date.class)) {
				value = rs.getTimestamp(fieldName);
			} else if (fieldType.equals(Boolean.class)
					|| fieldType.equals(boolean.class)) {
				value = rs.getBoolean(fieldName);
			} else if (fieldType.equals(BigDecimal.class)) {
				value = rs.getBigDecimal(fieldName);
			}  else if (fieldType.equals(byte[].class)) {
				Blob bb = rs.getBlob(fieldName);
				if (bb != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					IOUtil.copy(bb.getBinaryStream(), bos);
					value = bos.toByteArray();
				}
			} else {
				String strValue=rs.getString(fieldName);
				if(strValue!=null){
					value=JSONUtil.fromJson(strValue,fieldType);
				}
			}
			f.setAccessible(true);
			if (value != null) {
				f.set(o, value);
			}
		}
	}
	
	private boolean haveField(Class<?> type,String fieldName) {
		for (Field f : type.getFields()) {
			if(f.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	private QueryTerms createQueryTerms(AutoDomainQuery query) {
		QueryTerms qt=QueryTerms.create();
		query.queryParams.forEach((k,v)->{
			if(!haveField(query.domainClass,k)) {
				return;
			}
			String op=query.operators.get(k);
			if(op==null) {
				qt.where(convertFieldName(k),v);
			}else {
				qt.where(convertFieldName(k),op,v);
			}
		});
		query.autoDomainOrderBies.forEach(order->{
			String orderBy="";
			if(!haveField(query.domainClass, order.fieldName)) {
				return;
			}
			if(order.sort==AutoDomainOrderBy.SORT_ASC) {
				orderBy="asc";
			}
			if(order.sort==AutoDomainOrderBy.SORT_DESC) {
				orderBy="desc";
			}
			qt.orderBy(convertFieldName(order.fieldName)+" "+orderBy);
		});
		qt.limit(query.pageIndex*query.pageSize,query.pageSize);
		return qt;
	}
	//
	private class AutoDomainResultSetHandler implements ResultSetHandler<Object>{
		Class<?>clazz;
		public AutoDomainResultSetHandler(Class<?> clazz){
			this.clazz=clazz;
		}
		@Override
		public Object handleRow(ResultSet row) throws Exception {
			Object o=clazz.newInstance();
			convertBean(o,row);
			return o;
		}
		
	}
	//
	public Object getById(Class<?> type,int id) {
		QueryTerms qt=QueryTerms.create().where("id",id);
		return queryForObject(querySql(type,qt),
				new AutoDomainResultSetHandler(type), qt.whereValues());
	}

	
	public List<Object> queryList(Class<?>type,AutoDomainQuery query){
		QueryTerms qt=createQueryTerms(query);
		return queryForList(querySql(type,qt),new AutoDomainResultSetHandler(type), qt.whereValues());
	}
	
	private String queryCountSql(Class<?>type,QueryTerms qt){
		String tableName=getTableName(type);
		StringBuilder sql=new StringBuilder();
		sql.append("select count(1) ");
		sql.append(" from ").append(tableName);
		sql.append(" where 1=1");
		sql.append(qt.whereStatement());
		if(!qt.orderBys.isEmpty()){
			sql.append(" order by ");
			for(String k:qt.orderBys){
				sql.append(k).append(",");
			}
			sql.deleteCharAt(sql.length()-1);
		}
		return sql.toString();
	}
	
	public int queryCount(AutoDomainQuery query){
		QueryTerms qt=createQueryTerms(query);
		return queryForInteger(queryCountSql(query.domainClass,qt), qt.whereValues());
	}
}
