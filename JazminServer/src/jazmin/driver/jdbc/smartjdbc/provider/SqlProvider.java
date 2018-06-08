package jazmin.driver.jdbc.smartjdbc.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jazmin.driver.jdbc.smartjdbc.Config;
import jazmin.driver.jdbc.smartjdbc.SmartJdbcException;
import jazmin.driver.jdbc.smartjdbc.SqlBean;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainDefine;
import jazmin.driver.jdbc.smartjdbc.annotations.NonPersistent;
import jazmin.driver.jdbc.smartjdbc.annotations.PrimaryKey;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;
import jazmin.util.StringUtil;

/**
 * 
 * @author skydu
 *
 */
public abstract class SqlProvider {
	//
	private static Logger logger=LoggerFactory.getLogger(SqlProvider.class);
	public static final String MAIN_TABLE_ALIAS="a";
	//
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
	//
	protected static SqlBean createSqlBean(String sql,Object[] parameters) {
		SqlBean bean=new SqlBean(sql,parameters);	
		if(logger.isDebugEnabled()) {
			logger.debug("SqlBean {}",DumpUtil.dump(bean));
		}
		return bean;
	}
	
	/**
	 * 
	 * @param domainClass
	 * @return
	 */
	public static String getTableName(Class<?> domainClass) {
		Class<?> tableClass=domainClass;
		DomainDefine domainDefine=domainClass.getAnnotation(DomainDefine.class);
		if (domainDefine != null) {
			if(!StringUtil.isEmpty(domainDefine.tableName())) {//tableName first
				return domainDefine.tableName();
			}
			if(!domainDefine.domainClass().equals(void.class)){
				tableClass=domainDefine.domainClass();
			}
		}
		return Config.getTableName(tableClass);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String convertFieldName(String name) {
		return Config.convertFieldName(name);
	}

	/**
	 * 
	 * @param excludeProperties
	 * @param type
	 */
	public static void checkExcludeProperties(String []excludeProperties,Class<?>type){
		for(String p:excludeProperties){
			try {
				if(type.getField(p)==null){	
					return;
				}
			} catch (Exception e) {
				throw new SmartJdbcException("can not find property:"+
						p+" in type:"+type.getName());
			} 
		}
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public static List<Field> getPrimaryKey(Class<?> clazz){
		List<Field> primaryKey=new ArrayList<>();
		List<Field> fields=getPersistentFields(clazz);
		Field idField=null;
		for (Field field : fields) {
			if(field.getAnnotation(PrimaryKey.class)!=null) {
				primaryKey.add(field);
			}
			if(field.getName().equals("id")) {
				idField=field;
			}
		}
		if(primaryKey.size()==0&&idField==null) {
			throw new SmartJdbcException("PrimaryKey not found in "+clazz.getName());
		}
		if(primaryKey.size()==0) {
			return Arrays.asList(idField);
		}
		return primaryKey;
	}
	
	/**
	 * 
	 * @param domainClass
	 * @return
	 */
	public static List<Field> getPersistentFields(Class<?> domainClass){
		List<Field> fields=new ArrayList<>();
		for (Field field : domainClass.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if(field.getAnnotation(NonPersistent.class)!=null) {
				continue;
			}
			fields.add(field);
		}
		return fields;
	}
	
	/**
	 * 
	 * @param bean
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object bean,String fieldName){
		try {
			Field field=bean.getClass().getField(fieldName);
			if(field!=null) {
				return field.get(bean);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract SqlBean build();
}
