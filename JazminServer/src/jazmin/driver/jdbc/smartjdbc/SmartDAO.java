package jazmin.driver.jdbc.smartjdbc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.JazminDAO;
import jazmin.driver.jdbc.ResultSetHandler;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainField;
import jazmin.driver.jdbc.smartjdbc.annotations.NonPersistent;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryDefine;
import jazmin.driver.jdbc.smartjdbc.provider.DeleteProvider;
import jazmin.driver.jdbc.smartjdbc.provider.InsertProvider;
import jazmin.driver.jdbc.smartjdbc.provider.SelectProvider;
import jazmin.driver.jdbc.smartjdbc.provider.SqlProvider;
import jazmin.driver.jdbc.smartjdbc.provider.UpdateProvider;
import jazmin.util.IOUtil;
import jazmin.util.JSONUtil;
import jazmin.util.StringUtil;

/**
 * 
 * @author skydu
 */
public class SmartDAO extends JazminDAO{
	
	/**
	 * 
	 * @param o
	 * @param withGenerateKey
	 * @param excludeProperties
	 * @return
	 */
	public int insert(Object o,boolean withGenerateKey,String... excludeProperties){
		beforeInsert(o, withGenerateKey, excludeProperties);
		SqlBean sqlBean=new InsertProvider(o, excludeProperties).build();
		String sql=sqlBean.sql;
		Object[] parameters=sqlBean.parameters;
		int result=0;
		if(withGenerateKey){
			result=executeWithGenKey(sql,parameters);		
		}else{
			executeUpdate(sql,parameters);
		}
		afterInsert(result, o, withGenerateKey, excludeProperties);
		return result;
	}

	/**
	 * 
	 * @param o
	 * @param withGenerateKey
	 * @param excludeProperties
	 */
	protected void beforeInsert(Object o, boolean withGenerateKey, String[] excludeProperties) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeInsert(o, withGenerateKey, excludeProperties);
			}
		}
	}
	
	/**
	 * 
	 * @param result
	 * @param o
	 * @param withGenerateKey
	 * @param excludeProperties
	 */
	protected void afterInsert(int result, Object o, boolean withGenerateKey, String[] excludeProperties) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.afterInsert(result,o, withGenerateKey, excludeProperties);
			}
		}
	}
	
	/**
	 * 
	 * @param bean
	 * @param excludeProperties
	 * @return
	 */
	public int update(Object bean,
			String... excludeProperties){
		return update(bean,false,excludeProperties);
	}
	//
	/**
	 * 
	 * @param bean
	 * @param excludeNull
	 * @param excludeProperties
	 * @return
	 */
	public int update(Object bean,
			boolean excludeNull,
			String... excludeProperties){
		beforeUpdate(bean,excludeNull,excludeProperties);
		SqlBean sqlBean=new UpdateProvider(bean, excludeNull, excludeProperties).build();
		int result=executeUpdate(sqlBean.sql,sqlBean.parameters);
		afterUpdate(result,bean,excludeNull,excludeProperties);
		return result;
	}
	
	/**
	 * 
	 * @param bean
	 * @param excludeNull
	 * @param excludeProperties
	 */
	protected void beforeUpdate(Object bean, boolean excludeNull, String[] excludeProperties) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeUpdate(bean, excludeNull, excludeProperties);
			}
		}
	}
	
	/**
	 * 
	 * @param result
	 * @param bean
	 * @param excludeNull
	 * @param excludeProperties
	 */
	protected void afterUpdate(int result, Object bean, boolean excludeNull, String[] excludeProperties) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.afterUpdate(result,bean, excludeNull, excludeProperties);
			}
		}
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param qt
	 * @return
	 */
	public int delete(Class<?> domainClass,QueryWhere qw){
		beforeDelete(domainClass,qw);
		SqlBean sqlBean=new DeleteProvider(domainClass, qw).build();
		int result=executeUpdate(sqlBean.sql,sqlBean.parameters);
		afterDelete(result,domainClass,qw);
		return result;
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param qt
	 */
	protected void beforeDelete(Class<?> domainClass, QueryWhere qt) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeDelete(domainClass, qt);
			}
		}
	}

	/**
	 * 
	 * @param result
	 * @param domainClass
	 * @param qt
	 */
	protected void afterDelete(int result, Class<?> domainClass, QueryWhere qt) {
		List<DAOInterceptor> interceptors=SmartJdbcConfig.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.afterDelete(result,domainClass, qt);
			}
		}
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param qw
	 * @return
	 */
	public <T> T getDomain(Class<T> domainClass,QueryWhere qw){
		SqlBean sqlBean=new SelectProvider(domainClass).query(qw).build();
		return queryObject(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDomain(Query query){
		Class<T> domainClass=(Class<T>) getDomainClass(query);
		SqlBean sqlBean=new SelectProvider(domainClass).query(query).build();
		return queryObject(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param provider
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDomain(SelectProvider provider){
		SqlBean sqlBean=provider.build();
		Class<T> clazz=(Class<T>) provider.getDomainClass();
		return queryObject(clazz,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param qw
	 * @return
	 */
	public <T> List<T> getList(Class<T> domainClass,QueryWhere qw){
		SqlBean sqlBean=new SelectProvider(domainClass).query(qw).needPaging(true).build();
		return queryList(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public <T> List<T> getList(Query query){
		Class<T> domainClass=getDomainClass(query);
		SqlBean sqlBean=new SelectProvider(domainClass).query(query).needPaging(true).build();
		return queryList(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param selectProvider
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(SelectProvider selectProvider){
		Class<T> domainClass=(Class<T>) selectProvider.getDomainClass();
		SqlBean sqlBean=selectProvider.needPaging(true).build();
		return queryList(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param domainClass
	 * @return
	 */
	public <T> List<T> getAll(Class<T> domainClass){
		SqlBean sqlBean=new SelectProvider(domainClass).build();
		return queryList(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param qw
	 * @return
	 */
	public int getListCount(Class<?> domainClass,QueryWhere qw){
		SqlBean sqlBean=new SelectProvider(domainClass).
				selectCount().
				query(qw).
				needOrderBy(false).
				build();
		return queryForInteger(sqlBean.sql, sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public int getListCount(Query query){
		Class<?> domainClass=getDomainClass(query);
		SqlBean sqlBean=new SelectProvider(domainClass).
				selectCount().
				query(query).
				needOrderBy(false).
				build();
		return queryForInteger(sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param selectProvider
	 * @return
	 */
	public int getListCount(SelectProvider selectProvider){
		SqlBean sqlBean=selectProvider.selectCount().needOrderBy(false).build();
		return queryForInteger(sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getDomainClass(Query query) {
		QueryDefine queryDefine=query.getClass().getAnnotation(QueryDefine.class);
		if(queryDefine==null) {
			throw new SmartJdbcException("no domainClass found in QueryClass["+query.getClass().getName()+"]");
		}
		return (Class<T>) queryDefine.domainClass();
	}
	
	/**
	 * 
	 * @param type
	 * @param rs
	 * @return
	 */
	protected <T> T convertBean(Class<T> type,ResultSet rs){
		try{
			T instance=type.newInstance();
			convertBean(instance,rs);
			return instance;
		}catch(Exception e){
			throw new SmartJdbcException(e);
		}
	} 
	//
	protected void convertBean(Object o, ResultSet rs, String... excludeProperties)
			throws Exception {
		convertBean(o, null, rs, excludeProperties);
	}
	/**
	 * 
	 * @param o
	 * @param rs
	 * @param excludeProperties
	 * @throws Exception
	 */
	protected void convertBean(Object o,String preAliasField,ResultSet rs, String... excludeProperties)
			throws Exception {
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeProperties) {
			excludesNames.add(e);
		}
		Class<?> type = o.getClass();
		SqlProvider.checkExcludeProperties(excludeProperties,type);
		for (Field f : type.getFields()) {
			if (excludesNames.contains(f.getName())) {
				continue;
			}
			if(f.getAnnotation(NonPersistent.class)!=null) {
				continue;
			}
			String fieldName = convertFieldName(f.getName());
			if(preAliasField!=null) {
				fieldName=preAliasField+fieldName;
			}
			Class<?> fieldType = f.getType();
			if (Modifier.isStatic(f.getModifiers())||
					Modifier.isFinal(f.getModifiers())) {
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
				DomainField domainField=f.getAnnotation(DomainField.class);
				if(domainField==null||StringUtil.isEmpty(domainField.foreignKeyFields())) {
				String strValue=rs.getString(fieldName);
				if(strValue!=null){
					Type genericType=f.getGenericType();
					if ( genericType instanceof ParameterizedType ) {  
						 Type[] typeArguments = ((ParameterizedType)genericType).getActualTypeArguments();  
						 if(typeArguments.length==1) {
							 if(List.class.isAssignableFrom(fieldType) && (typeArguments[0] instanceof Class)) {
								 value=JSONUtil.fromJsonList(strValue,(Class<?>) typeArguments[0]);
							 }
						 }
					 }else {
						 value=JSONUtil.fromJson(strValue,fieldType);
					 }
				}
				}else {
					Class<?> subClass=((Class<?>)f.getGenericType());
					value=subClass.newInstance();
					String subPreAliasField=f.getName()+"_";
					convertBean(value, subPreAliasField, rs, excludeProperties);
				}
			}
			f.setAccessible(true);
			if (value != null) {
				f.set(o, value);
			}
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	protected  String convertFieldName(String name) {
		return SmartJdbcConfig.convertFieldName(name);
	}
	
	/**
	 * 
	 * @param sql
	 * @param rowHandler
	 * @param parameters
	 * @return
	 */
	public <T> List<T> queryList(
			String sql,
			ResultSetHandler<T> rowHandler, 
			Object... parameters) {
		return queryForList(sql, rowHandler, parameters);
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public <T> List<T> queryList(
			Class<T> domainClass,
			String sql,
			Object... parameters) {
		return queryForList(sql, new ResultSetHandler<T>() {
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=domainClass.newInstance();
				convertBean(o,row);
				return o;
			}}, parameters);
	}
	/**
	 * 
	 * @param domainClass
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public final <T> T queryObject(
			Class<T> domainClass,
			String sql,
			Object... parameters) {
		return queryForObject(sql, new ResultSetHandler<T>() {
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=domainClass.newInstance();
				convertBean(o,row);
				return o;
			}}, parameters);
	}
	
	/**
	 * 
	 * @param sql
	 * @param rowHandler
	 * @param parameters
	 * @return
	 */
	public final <T> T queryObject(
			String sql,
			ResultSetHandler<T> rowHandler, 
			Object... parameters) {
		return queryForObject(sql, rowHandler, parameters);
	}
	
	/**
	 * 
	 * @param domainClass
	 * @param clazz
	 * @param field
	 * @param qt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <S extends Number>S sum(Class<?> domainClass,Class<S> clazz,String field,QueryWhere qt){
		SqlBean sqlBean=new SelectProvider(domainClass).sum(field).needOrderBy(false).build();
		String sql=sqlBean.sql;
		Object[] parameters=sqlBean.parameters;
		if(clazz==long.class||clazz==Long.class){
			return (S) queryForLong(sql,parameters);
		}
		if(clazz==int.class||clazz==Integer.class){
			return (S) queryForInteger(sql,parameters);
		}
		if(clazz==short.class||clazz==Short.class){
			return (S) queryForShort(sql,parameters);
		}
		if(clazz==Double.class||clazz==Double.class){
			return (S) queryForDouble(sql,parameters);
		}
		if(clazz==Float.class||clazz==Float.class){
			return (S) queryForFloat(sql,parameters);
		}
		throw new IllegalArgumentException(clazz.getSimpleName()+" not supported");
	}
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public final Integer queryInteger(String sql,Object ...parameters){
		return  queryForInteger(sql, parameters);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public final int executeForUpdate(String sql,Object... parameters) {
		return executeUpdate(sql, parameters);
	}
}
