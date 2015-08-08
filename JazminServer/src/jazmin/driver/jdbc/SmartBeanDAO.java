/**
 * 
 */
package jazmin.driver.jdbc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.QueryTerms.Where;
import jazmin.util.IOUtil;

/**
 * @author yama
 *
 */
public class SmartBeanDAO<T> extends JazminDAO {
	
	protected Class<?>getTypeClass(){
		ParameterizedType pt=(ParameterizedType) getClass().getGenericSuperclass();
		Class<?>type=(Class<?>) pt.getActualTypeArguments()[0];
		return type;
	}
	//
	protected int update(T bean,
			QueryTerms qt,
			String... excludeProperties){
		return update(bean,false,qt,excludeProperties);
	}
	//
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
	//
	protected int update(T bean,
			boolean excludeNull,
			QueryTerms qt,
			String... excludeProperties){
		
		StringBuilder sql=new StringBuilder();
		Class<?>type=getTypeClass();
		checkExcludeProperties(excludeProperties,type);
		String tableName="t"+convertFieldName(type.getSimpleName());
		sql.append("update ").append(tableName).append(" ");
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeProperties) {
			excludesNames.add(e);
		}
		List<Object>fieldList=new ArrayList<Object>();
		sql.append("set ");
		for (Field f : type.getFields()) {
			if (excludesNames.contains(f.getName())) {
				continue;
			}
			String fieldName = convertFieldName(f.getName());
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			try {
				Object fieldValue=f.get(bean);
				if(excludeNull&&fieldValue==null){
					continue;
				}
				fieldList.add(f.get(bean));
			} catch (Exception e) {
				throw new ConnectionException(e);
			}
			sql.append(" `").append(fieldName).append("`=?,");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" where 1=1");
		for(Where k:qt.wheres){
			getWhereStatement(sql,k);
			fieldList.add(k.value);
		}
		return executeUpdate(sql.toString(), 
				fieldList.toArray(new Object[fieldList.size()]));
	}
	//
	protected int delete(QueryTerms qt){
		StringBuilder sql=new StringBuilder();
		Class<?>type=getTypeClass();
		String tableName="t"+convertFieldName(type.getSimpleName());
		sql.append("delete from ").append(tableName);
		sql.append(" where 1=1");
		for(Where k:qt.wheres){
			getWhereStatement(sql,k);
		}
		return executeUpdate(sql.toString(),qt.whereValues());
	}
	//
	private String querySql(QueryTerms qt,String... excludeProperties){
		StringBuilder sql=new StringBuilder();
		Class<?>type=getTypeClass();
		checkExcludeProperties(excludeProperties,type);
		String tableName="t"+convertFieldName(type.getSimpleName());
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
		for(Where k:qt.wheres){
			getWhereStatement(sql,k);
		}
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
	//
	private void getWhereStatement(StringBuilder sql,Where w){
		sql.append(" and ");
		sql.append("`").append(w.key).append("` ");
		sql.append(w.operator).append(" ");
		if(w.operator.trim().equalsIgnoreCase("like")){
			sql.append(" concat('%',?,'%') ");
		}else{
			sql.append(" ? ");
		}
	}
	//
	protected T query(QueryTerms qt,String... excludeProperties){
		Class<?>type=getTypeClass();
		return queryForObject(querySql(qt,excludeProperties),new ResultSetHandler<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=(T) type.newInstance();
				convertBean(o, row, excludeProperties);
				return o;
			}			
		}, qt.whereValues());
	}
	//
	protected List<T>queryList(QueryTerms qt,String... excludeProperties){
		Class<?>type=getTypeClass();
		return queryForList(querySql(qt,excludeProperties),new ResultSetHandler<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=(T) type.newInstance();
				convertBean(o, row, excludeProperties);
				return o;
			}			
		}, qt.whereValues());
	}
	//
	protected int insert(T o,boolean withGenerateKey,String... excludeProperties){
		StringBuilder sql=new StringBuilder();
		Class<?>type=getTypeClass();
		checkExcludeProperties(excludeProperties,type);
		String tableName="t"+convertFieldName(type.getSimpleName());
		sql.append("insert into ").append(tableName).append("(");
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeProperties) {
			excludesNames.add(e);
		}
		List<Object>fieldList=new ArrayList<Object>();
		for (Field f : type.getFields()) {
			if (excludesNames.contains(f.getName())) {
				continue;
			}
			String fieldName = convertFieldName(f.getName());
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			try {
				fieldList.add(f.get(o));
			} catch (Exception e) {
				throw new ConnectionException(e);
			}
			sql.append("`").append(fieldName).append("`,");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(")");
		sql.append("values(");
		for(int i=0;i<fieldList.size();i++){
			sql.append("?,");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(")");
		if(withGenerateKey){
			return executeWithGenKey(sql.toString(),
					fieldList.toArray(new Object[fieldList.size()]));		
		}else{
			execute(sql.toString(),
					fieldList.toArray(new Object[fieldList.size()]));
			return 0;
		}
	}
	//
	@SuppressWarnings("unchecked")
	protected T convertBean(ResultSet rs){
		Class<?>type=getTypeClass();
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
			} else if (fieldType.equals(byte[].class)) {
				Blob bb = rs.getBlob(fieldName);
				if (bb != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					IOUtil.copy(bb.getBinaryStream(), bos);
					value = bos.toByteArray();
				}
			} else {
				throw new IllegalArgumentException("bad field type:"
						+ fieldName + "/" + fieldType);
			}
			f.setAccessible(true);
			if (value != null) {
				f.set(o, value);
			}
		}
	}
	//
	private static String convertFieldName(String name) {
		StringBuffer result = new StringBuffer();
		for (char c : name.toCharArray()) {
			if (Character.isUpperCase(c)) {
				result.append("_");
			}
			result.append(Character.toLowerCase(c));
		}
		return result.toString();
	}
}
