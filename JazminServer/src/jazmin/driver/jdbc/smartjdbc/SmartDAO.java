package jazmin.driver.jdbc.smartjdbc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.JazminDAO;
import jazmin.driver.jdbc.ResultSetHandler;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainField;
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
	 * @param excludeFields
	 * @return
	 */
	public int insert(Object o,boolean withGenerateKey,String... excludeFields){
		beforeInsert(o, withGenerateKey, excludeFields);
		SqlBean sqlBean=new InsertProvider(o, excludeFields).build();
		String sql=sqlBean.sql;
		Object[] parameters=sqlBean.parameters;
		int result=0;
		if(withGenerateKey){
			result=executeWithGenKey(sql,parameters);		
		}else{
			executeUpdate(sql,parameters);
		}
		afterInsert(result, o, withGenerateKey, excludeFields);
		return result;
	}

	/**
	 * 
	 * @param o
	 * @param withGenerateKey
	 * @param excludeFields
	 */
	protected void beforeInsert(Object o, boolean withGenerateKey, String[] excludeFields) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeInsert(o, withGenerateKey, excludeFields);
			}
		}
	}
	
	/**
	 * 
	 * @param result
	 * @param o
	 * @param withGenerateKey
	 * @param excludeFields
	 */
	protected void afterInsert(int result, Object o, boolean withGenerateKey, String[] excludeFields) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.afterInsert(result,o, withGenerateKey, excludeFields);
			}
		}
	}
	
	/**
	 * 
	 * @param bean
	 * @param excludeFields
	 * @return
	 */
	public int update(Object bean,
			String... excludeFields){
		return update(bean,false,excludeFields);
	}
	//
	/**
	 * 
	 * @param bean
	 * @param excludeNull
	 * @param excludeFields
	 * @return
	 */
	public int update(Object bean,
			boolean excludeNull,
			String... excludeFields){
		beforeUpdate(bean,excludeNull,excludeFields);
		SqlBean sqlBean=new UpdateProvider(bean, excludeNull, excludeFields).build();
		int result=executeUpdate(sqlBean.sql,sqlBean.parameters);
		afterUpdate(result,bean,excludeNull,excludeFields);
		return result;
	}
	
	/**
	 * 
	 * @param bean
	 * @param excludeNull
	 * @param excludeFields
	 */
	protected void beforeUpdate(Object bean, boolean excludeNull, String[] excludeFields) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeUpdate(bean, excludeNull, excludeFields);
			}
		}
	}
	
	/**
	 * 
	 * @param result
	 * @param bean
	 * @param excludeNull
	 * @param excludeFields
	 */
	protected void afterUpdate(int result, Object bean, boolean excludeNull, String[] excludeFields) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.afterUpdate(result,bean, excludeNull, excludeFields);
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
	 * @param qw
	 */
	protected void beforeDelete(Class<?> domainClass, QueryWhere qw) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeDelete(domainClass, qw);
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
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
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
	public <T> T getDomain(Class<T> domainClass,QueryWhere qw,String ... excludeFields){
		SqlBean sqlBean=new SelectProvider(domainClass).query(qw).
				excludeFields(excludeFields).build();
		return queryObject(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 */
	protected void beforeQuery(Query query) {
		List<DAOInterceptor> interceptors=Config.getDaoInterceptors();
		if(interceptors!=null) {
			for (DAOInterceptor interceptor : interceptors) {
				interceptor.beforeQuery(query);
			}
		}
	}
	/**
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDomain(Query query,String ... excludeFields){
		beforeQuery(query);
		Class<T> domainClass=(Class<T>) getDomainClass(query);
		SqlBean sqlBean=new SelectProvider(domainClass).query(query).
				excludeFields(excludeFields).build();
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
	public <T> List<T> getList(Class<T> domainClass,QueryWhere qw,String ... excludeFields){
		SqlBean sqlBean=new SelectProvider(domainClass).query(qw).
				excludeFields(excludeFields).needPaging(true).build();
		return queryList(domainClass,sqlBean.sql,sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public <T> List<T> getList(Query query,String ... excludeFields){
		beforeQuery(query);
		Class<T> domainClass=getDomainClass(query);
		SqlBean sqlBean=new SelectProvider(domainClass).query(query).
				excludeFields(excludeFields).needPaging(true).build();
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
	public <T> List<T> getAll(Class<T> domainClass,String ... excludeFields){
		SqlBean sqlBean=new SelectProvider(domainClass).excludeFields(excludeFields).build();
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
		beforeQuery(query);
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
	protected void convertBean(Object o, ResultSet rs, String... excludeFields)
			throws Exception {
		convertBean(o, null, rs, excludeFields);
	}
	///
	protected static final HashSet<Class<?>> WRAP_TYPES=new HashSet<>();
	static{
		WRAP_TYPES.add(Boolean.class);
		WRAP_TYPES.add(Character.class);
		WRAP_TYPES.add(Byte.class);
		WRAP_TYPES.add(Short.class);
		WRAP_TYPES.add(Integer.class);
		WRAP_TYPES.add(Long.class);
		WRAP_TYPES.add(BigDecimal.class);
		WRAP_TYPES.add(BigInteger.class);
		WRAP_TYPES.add(Double.class);
		WRAP_TYPES.add(Float.class);
		WRAP_TYPES.add(String.class);
		WRAP_TYPES.add(Date.class);
		WRAP_TYPES.add(Timestamp.class);
		WRAP_TYPES.add(java.sql.Date.class);
		WRAP_TYPES.add(Byte[].class);
		WRAP_TYPES.add(byte[].class);
		WRAP_TYPES.add(int.class);
		WRAP_TYPES.add(boolean.class);
		WRAP_TYPES.add(char.class);
		WRAP_TYPES.add(byte.class);
		WRAP_TYPES.add(short.class);
		WRAP_TYPES.add(int.class);
		WRAP_TYPES.add(long.class);
		WRAP_TYPES.add(float.class);
		WRAP_TYPES.add(double.class);
	}
	/**
	 * 
	 * @param o
	 * @param rs
	 * @param excludeFields
	 * @throws Exception
	 */
	protected void convertBean(Object o,String preAliasField,ResultSet rs, String... excludeFields)
			throws Exception {
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeFields) {
			excludesNames.add(e);
		}
		Class<?> type = o.getClass();
		SqlProvider.checkExcludeProperties(excludeFields,type);
		//
		ResultSetMetaData rsmd=rs.getMetaData();
		int columnCount=rsmd.getColumnCount();
		Set<String> columnNames=new HashSet<>();
		for(int i=1;i<=columnCount;i++) {
			columnNames.add(rsmd.getColumnLabel(i));
		}
		for (Field f : type.getFields()) {
			if (excludesNames.contains(f.getName())) {
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
			if(!columnNames.contains(fieldName)) {
				if(WRAP_TYPES.contains(fieldType)){
					continue;
				}
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
					convertBean(value, subPreAliasField, rs, excludeFields);
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
		return Config.convertFieldName(name);
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
		SqlBean sqlBean=parseSql(sql, parameters);
		return queryForList(sqlBean.sql, rowHandler, sqlBean.parameters);
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
		SqlBean sqlBean=parseSql(sql, parameters);
		return queryForList(sqlBean.sql, new ResultSetHandler<T>() {
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=domainClass.newInstance();
				convertBean(o,row);
				return o;
			}}, sqlBean.parameters);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int queryCount(
			String sql,
			Object... parameters) {
		SqlBean sqlBean=parseSql(sql, parameters);
		return queryForInteger(sqlBean.sql, sqlBean.parameters);
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
		SqlBean sqlBean=parseSql(sql, parameters);
		return queryForObject(sqlBean.sql, new ResultSetHandler<T>() {
			@Override
			public T handleRow(ResultSet row) throws Exception {
				T o=domainClass.newInstance();
				convertBean(o,row);
				return o;
			}}, sqlBean.parameters);
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
		SqlBean sqlBean=parseSql(sql, parameters);
		return queryForObject(sqlBean.sql, rowHandler, sqlBean.parameters);
	}
	
	
	public SqlBean parseSql(String sql,Object... parameters) {
		if(!SelectProvider.preParseSql(sql)) {
			return new SqlBean(sql, parameters);
		}
		Map<String,Object> paraMap=new HashMap<>();
		if(parameters!=null) {
			for (Object para : parameters) {
				if(para instanceof SqlParam) {
					SqlParam p=(SqlParam) para;
					if(StringUtil.isEmpty(p.name)){
						throw new SmartJdbcException("Param name cann't be null");
					}
					paraMap.put("#{"+p.name+"}", p.value);
				}
			}
		}
		return SelectProvider.parseSql(sql, paraMap);//#
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
	public final int executeUpdate(String sql,Object... parameters) {
		SqlBean sqlBean=parseSql(sql, parameters);
		return super.executeUpdate(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Boolean queryForBoolean(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForBoolean(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final String queryForString(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForString(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Double queryForDouble(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForDouble(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Float queryForFloat(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForFloat(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Integer queryForInteger(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForInteger(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Long queryForLong(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForLong(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Short queryForShort(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForShort(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final BigDecimal queryForBigDecimal(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForBigDecimal(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final Byte queryForByte(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForByte(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final  Date queryForDate(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForDate(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Boolean> queryForBooleans(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForBooleans(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<String> queryForStrings(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForStrings(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Double> queryForDoubles(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForDoubles(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Float> queryForFloats(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForFloats(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Integer> queryForIntegers(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForIntegers(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Long> queryForLongs(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForLongs(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Short> queryForShorts(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForShorts(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<BigDecimal> queryForBigDecimals(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForBigDecimals(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Byte> queryForBytes(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForBytes(sqlBean.sql, sqlBean.parameters);
	}
	//
	public final List<Date> queryForDates(String sql,Object ...parameters){
		SqlBean sqlBean=parseSql(sql, parameters);
		return  super.queryForDates(sqlBean.sql, sqlBean.parameters);
	}
	//
}
