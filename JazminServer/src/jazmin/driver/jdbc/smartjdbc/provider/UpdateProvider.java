package jazmin.driver.jdbc.smartjdbc.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.smartjdbc.QueryWhere;
import jazmin.driver.jdbc.smartjdbc.SmartJdbcException;
import jazmin.driver.jdbc.smartjdbc.SqlBean;
import jazmin.driver.jdbc.smartjdbc.annotations.NonPersistent;
import jazmin.util.JSONUtil;

/**
 * 
 * @author skydu
 *
 */
public class UpdateProvider extends SqlProvider{
	//
	Object bean;
	QueryWhere qw;
	String[] excludeProperties;
	boolean excludeNull;
	//
	public UpdateProvider(Object bean,boolean excludeNull,String ... excludeProperties) {
		this(bean, null, excludeNull, excludeProperties);
	}
	//
	public UpdateProvider(Object bean,QueryWhere qw,boolean excludeNull,String ... excludeProperties) {
		this.bean=bean;
		this.qw=qw;
		this.excludeNull=excludeNull;
		this.excludeProperties=excludeProperties;
	}
	//
	@Override
	public SqlBean build() {
		StringBuilder sql=new StringBuilder();
		Class<?>type=bean.getClass();
		checkExcludeProperties(excludeProperties,type);
		String tableName=getTableName(type);
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
			NonPersistent nonPersistent=f.getAnnotation(NonPersistent.class);
			if(nonPersistent!=null) {
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
				if(fieldValue!=null&&!WRAP_TYPES.contains(fieldValue.getClass())){
					fieldList.add(JSONUtil.toJson(fieldValue));
				}else{
					fieldList.add(fieldValue);
				}
			} catch (Exception e) {
				throw new SmartJdbcException(e);
			}
			sql.append(" `").append(fieldName).append("`=?,");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" where 1=1");
		if(qw==null) {//默认where主键
			qw=QueryWhere.create();
			List<Field> primaryKey=getPrimaryKey(bean.getClass());
			for (Field field : primaryKey) {
				qw.where(convertFieldName(field.getName()),getFieldValue(bean, field.getName()));
			}
		}
		sql.append(qw.whereStatement());
		for(Object o:qw.whereValues()){
			fieldList.add(o);
		}
		return createSqlBean(sql.toString(), fieldList.toArray(new Object[fieldList.size()]));
	}

}
