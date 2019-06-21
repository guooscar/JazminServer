package jazmin.driver.jdbc.smartjdbc.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.jdbc.smartjdbc.QueryWhere;
import jazmin.driver.jdbc.smartjdbc.QueryWhere.WhereStatment;
import jazmin.driver.jdbc.smartjdbc.SmartJdbcException;
import jazmin.driver.jdbc.smartjdbc.SqlBean;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainField;
import jazmin.util.ClassUtils;
import jazmin.util.JSONUtil;

/**
 * 
 * @author skydu
 *
 */
public class UpdateProvider extends SqlProvider{
	//
	protected Object bean;
	protected QueryWhere qw;
	protected Set<String> includeFields;
	protected String[] excludeFields;
	protected boolean excludeNull;
	//
	public UpdateProvider(Object bean,boolean excludeNull,Set<String> includeFields,String ... excludeFields) {
		this(bean, null, excludeNull, includeFields,excludeFields);
	}
	//
	public UpdateProvider(Object bean,QueryWhere qw,boolean excludeNull,Set<String> includeFields,String ... excludeFields) {
		this.bean=bean;
		this.qw=qw;
		this.excludeNull=excludeNull;
		this.includeFields=includeFields;
		this.excludeFields=excludeFields;
	}
	//
	@Override
	public SqlBean build() {
		StringBuilder sql=new StringBuilder();
		Class<?>type=bean.getClass();
		checkExcludeProperties(excludeFields,type);
		String tableName=getTableName(type);
		sql.append("update ").append(tableName).append(" ");
		Set<String> excludesNames = new TreeSet<String>();
		for (String e : excludeFields) {
			excludesNames.add(e);
		}
		List<Object>fieldList=new ArrayList<Object>();
		sql.append("set ");
		List<Field> fields = ClassUtils.getFieldList(type);
		for (Field f : fields) {
			if(includeFields!=null&&!includeFields.isEmpty()&&(!includeFields.contains(f.getName()))){
				continue;
			}
			if (excludesNames.contains(f.getName())) {
				continue;
			}
			if(!isPersistentField(f)) {
				continue;
			}
			DomainField domainField=f.getAnnotation(DomainField.class);
			if(domainField!=null&&domainField.autoIncrement()) {
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
		WhereStatment ws=qw.whereStatement();
		sql.append(ws.sql);
		for(Object o:ws.values){
			fieldList.add(o);
		}
		return createSqlBean(sql.toString(), fieldList.toArray(new Object[fieldList.size()]));
	}

}
