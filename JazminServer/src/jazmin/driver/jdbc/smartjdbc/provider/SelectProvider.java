package jazmin.driver.jdbc.smartjdbc.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jazmin.driver.jdbc.smartjdbc.Config;
import jazmin.driver.jdbc.smartjdbc.Query;
import jazmin.driver.jdbc.smartjdbc.QueryFieldInfo;
import jazmin.driver.jdbc.smartjdbc.QueryInfo;
import jazmin.driver.jdbc.smartjdbc.QueryWhere;
import jazmin.driver.jdbc.smartjdbc.QueryWhere.Where;
import jazmin.driver.jdbc.smartjdbc.SmartJdbcException;
import jazmin.driver.jdbc.smartjdbc.SqlBean;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainField;
import jazmin.driver.jdbc.smartjdbc.annotations.ForeignKey;
import jazmin.driver.jdbc.smartjdbc.annotations.InnerJoin;
import jazmin.driver.jdbc.smartjdbc.annotations.InnerJoins;
import jazmin.driver.jdbc.smartjdbc.annotations.LeftJoin;
import jazmin.driver.jdbc.smartjdbc.annotations.OrderBys;
import jazmin.driver.jdbc.smartjdbc.annotations.OrderBys.OrderBy;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryDefine;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryField.OrGroup;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.ArrayUtils;
import jazmin.util.ClassUtils;
import jazmin.util.StringUtil;

/**
 * 
 * @author skydu
 *
 */
public class SelectProvider extends SqlProvider{
	//
	private static Logger logger=LoggerFactory.getLogger(SelectProvider.class);
	//
	public static class SelectField{//select tableAlias.field as asField
		public String tableAlias;
		public String field;
		public String preAsField;
		public String asField;
		public boolean distinct;
		public String statFunction;
	}
	//
	public static class GroupByField{
		public String tableAlias;
		public String field;
	}
	//
	public static class Join{
		public String key;
		public String table1Alias;
		public Class<?> table1;
		public String table1Field;
		public String table2Alias;
		public Class<?> table2;
		public String table2Field;
		public List<Join> joins;
		//
		public Join() {
			joins=new ArrayList<>();
		}
	}
	//
	public static class SortField {
		public String fieldName;
		public int sortType;
		public int order;
	}
	//
	protected Class<?> domainClass;
	protected Query query;
	protected boolean isSelectCount;
	protected boolean needPaging;
	protected boolean needOrderBy;
	protected boolean isForUpdate;
	protected List<SelectField> selectFields;
	protected boolean ingoreSelectDomainFiled;
	protected Set<String> includeFields;
	protected Set<String> excludeFields;//userName not user_name
	protected Map<String,Join> innerJoinMap;
	protected Map<String,String> innerJoinFieldAliasMap;
	protected List<Join> innerJoins;//inner join tableName alias on 
	protected List<Join> leftJoins;//left join tableName alias on 
	protected QueryWhere qw;
	protected List<GroupByField> groupBys;
	//
	public SelectProvider(Class<?> domainClass) {
		this.domainClass=domainClass;
		this.selectFields=new ArrayList<>();
		this.includeFields=new LinkedHashSet<>();
		this.excludeFields=new LinkedHashSet<>();
		this.qw=QueryWhere.create();
		this.groupBys=new ArrayList<>();
		this.leftJoins=new ArrayList<>();
		this.innerJoins=new ArrayList<>();
		this.needOrderBy=true;
	}
	//
	public SelectProvider selectCount() {
		this.isSelectCount=true;
		return this;
	}
	//
	public SelectProvider sum(String sumField) {
		sum(MAIN_TABLE_ALIAS, sumField, sumField);
		return this;
	}
	//
	public SelectProvider sum(String alias,String field,String asField) {
		select(alias, field,null,asField, false, "sum");
		return this;
	}
	//
	public SelectProvider ingoreSelectDomainFiled() {
		this.ingoreSelectDomainFiled=true;
		return this;
	}
	//
	public SelectProvider needPaging(boolean needPaging) {
		this.needPaging=needPaging;
		return this;
	}
	//
	public SelectProvider needOrderBy(boolean needOrderBy) {
		this.needOrderBy=needOrderBy;
		return this;
	}
	//
	public SelectProvider query(Query query) {
		this.query=query;
		return this;
	}
	//
	public SelectProvider query(QueryWhere qw) {
		this.qw=qw;
		return this;
	}
	//
	public SelectProvider select(String field) {
		return select(null, field);
	}
	//
	public SelectProvider select(String tableAlias,String field) {
		return select(tableAlias,field,null);
	}
	//
	public SelectProvider select(String tableAlias,String field,String asAlias) {
		return select(tableAlias, field,null,asAlias, false, null);
	}
	//
	public SelectProvider select(String tableAlias,String field,
			String preAsField,String asField,boolean distinct,String statFunction) {
		selectFields.add(createSelectField(tableAlias, field, 
				preAsField,asField, distinct, statFunction));
		return this;
	}
	//
	public SelectProvider includeFields(Set<String> fields){
		if(fields!=null) {
			for (String field : fields) {
				includeFields.add(field);
			}
		}
		return this;
	}
	//
	public SelectProvider excludeFields(String ... fields){
		if(fields!=null) {
			for (String field : fields) {
				excludeFields.add(field);
			}
		}
		return this;
	}
	//
	protected SelectField createSelectField(String tableAlias,String field,
			String preAsField,String asField,boolean distinct,String statFunction) {
		SelectField sf=new SelectField();
		sf.tableAlias=tableAlias;
		sf.field=field;
		sf.preAsField=preAsField;
		sf.asField=asField;
		sf.distinct=distinct;
		sf.statFunction=statFunction;
		return sf;
	}
	//
	public SelectProvider where(String alias,String key,Object value){
		return this.where(alias,key, "=", value);
	}
	//
	public SelectProvider where(String key,Object value){
		return this.where(MAIN_TABLE_ALIAS,key, "=", value);
	}
	//
	public SelectProvider where(String alias,String key,String op,Object value){
		return where(alias, key, op, value, null);
	}
	//
	public SelectProvider where(String alias,String key,String op,Object value,OrGroup orGroup){
		qw.where(alias, key, op, value,orGroup);
		return this;
	}
	//
	public SelectProvider inOrNotin(String alias,String operator,String key,Object[] values){
		if(StringUtil.isEmpty(operator)||operator.trim().equalsIgnoreCase("in")) {
			qw.in(alias, key, values);
		}
		else if(operator.trim().equalsIgnoreCase("not in")) {
			qw.notin(alias, key, values);
		}
		return this;
	}
	//
	public SelectProvider in(String alias,String key,Object[] values){
		qw.in(alias, key, values);
		return this;
	}
	//
	public SelectProvider notin(String alias,String key,Object[] values){
		qw.notin(alias, key, values);
		return this;
	}
	//
	public SelectProvider whereSql(String sql,Object ...values){
		qw.whereSql(sql,null, values);
		return this;
	}
	//
	public SelectProvider whereSql(String sql,OrGroup orGroup,Object ...values){
		qw.whereSql(sql,orGroup, values);
		return this;
	}
	//
	public SelectProvider groupBy(String field) {
		groupBy(MAIN_TABLE_ALIAS, field);
		return this;
	}
	//
	public SelectProvider groupBy(String tableAlias,String field) {
		groupBys.add(createGroupByField(tableAlias, field));
		return this;
	}
	//
	protected GroupByField createGroupByField(String tableAlias,String field) {
		GroupByField groupByField=new GroupByField();
		groupByField.tableAlias=tableAlias;
		groupByField.field=field;
		return groupByField;
	}
	//
	public SelectProvider orderBy(String orderBy){
		qw.orderBy(orderBy);
		return this;
	}
	//
	public SelectProvider limit(int start,int limit){
		qw.limit(start, limit);
		return this;
	}
	//
	public SelectProvider limit(int end){
		qw.limit(end);
		return this;
	}
	//
	public SelectProvider forUpdate(){
		this.isForUpdate=true;
		return this;
	}
	//
	protected List<Field> getQueryFields(Query query){
		List<Field> fieldList=new ArrayList<>();
		List<Field> fields = ClassUtils.getFieldList(query.getClass());
		QueryDefine queryDefine = query.getClass().getAnnotation(QueryDefine.class);
		if (queryDefine == null) {
			throw new IllegalArgumentException("queryDefine not found in " + query.getClass().getName());
		}
		for (Field field : fields) {
			try {
				if (Modifier.isStatic(field.getModifiers()) || 
						Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				if(field.getType().equals(int.class)&&field.getName().endsWith("Sort")) {//ingore Sort Field
					continue;
				}
				Class<?> fieldType = field.getType();
				Object reallyValue = field.get(query);
				if (reallyValue == null
						|| (fieldType.equals(String.class) && StringUtil.isEmpty((String) reallyValue))) {
					continue;
				}
				QueryField queryField = field.getAnnotation(QueryField.class);
				if (queryField!= null && queryField.ingore()) {
					continue;
				}
				fieldList.add(field);
			}catch (Exception e) {
				logger.error(e.getMessage(),e);
				throw new IllegalArgumentException(e);
			}
		}
		return fieldList;
	}
	//
	protected boolean isValidInnerJoin(InnerJoin innerJoin) {
		if(innerJoin==null) {
			return false;
		}
		if(innerJoin.table2().equals(void.class)) {
			throw new SmartJdbcException("@InnerJoin table2 cannot be null");
		}
		if(StringUtil.isEmpty(innerJoin.table1Field())) {
			throw new SmartJdbcException("@InnerJoin table1Field cannot be null");
		}
		if(StringUtil.isEmpty(innerJoin.table2Field())) {
			throw new SmartJdbcException("@InnerJoin table2Field cannot be null");
		}
		return true;
	}
	//
	protected Map<String, Join> getInnerJoins(Query query) {
		Map<String, Join> map = new LinkedHashMap<>();
		if(query==null) {
			return map;
		}
		Class<?> queryClass=query.getClass();
		QueryDefine qd=queryClass.getAnnotation(QueryDefine.class);
		if(qd==null||qd.domainClass().equals(void.class)) {
			throw new IllegalArgumentException("domainClass not found /"+query.getClass().getSimpleName());
		}
		List<Field> fields=getQueryFields(query);
		int index = 1;
		innerJoinFieldAliasMap=new HashMap<>();
		for (Field field : fields) {
			InnerJoin innerJoin=field.getAnnotation(InnerJoin.class);
			InnerJoins innerJoins=field.getAnnotation(InnerJoins.class);
			QueryField queryField=field.getAnnotation(QueryField.class);
			String foreignKeyFields="";
			if(queryField!=null) {
				foreignKeyFields=queryField.foreignKeyFields();
			}
			if(innerJoin==null&&innerJoins==null&&StringUtil.isEmpty(foreignKeyFields)) {
				continue;
			}
			List<InnerJoin> innerJoinsList=new ArrayList<>();
			if(isValidInnerJoin(innerJoin)) {
				innerJoinsList.add(innerJoin);
			}
			if(innerJoins!=null&&innerJoins.innerJoins()!=null) {
				for (InnerJoin join : innerJoins.innerJoins()) {
					if(isValidInnerJoin(join)) {
						innerJoinsList.add(join);
					}
				}
			}
			if(innerJoinsList.size()>0) {//use annotation
				Join join=null;
				Class<?> table1=domainClass;
				String table1Alias=MAIN_TABLE_ALIAS;
				for (InnerJoin j: innerJoinsList) {
					String key=j.table1Field()+"-"+j.table2().getName()+"-"+j.table2Field();
					if(join==null) {
						join = map.get(key);
						if(join==null) {
							join=createInnerJoin(key,table1Alias,"i"+(index++),table1,j.table2(),j.table1Field(),
									j.table2Field());
							map.put(key, join);
						}
					}else {
						Join childJoin=getJoin(key, join.joins);
						if(childJoin==null) {
							childJoin=createInnerJoin(key,table1Alias,"i"+(index++),table1,j.table2(),j.table1Field(),
									j.table2Field());
							join.joins.add(childJoin);
						}
						join=childJoin;
					}
					table1=join.table2;
					table1Alias=join.table2Alias;
				}
				innerJoinFieldAliasMap.put(field.getName(), table1Alias);
			}else if(!StringUtil.isEmpty(foreignKeyFields)) {
				String[] foreignKeyIds=foreignKeyFields.split(",");
				Class<?> table1=domainClass;
				String table1Alias=MAIN_TABLE_ALIAS;
				Join join=null;
				for (String id : foreignKeyIds) {
					Field foreignKeyField=null;
					try {
						foreignKeyField=table1.getField(id);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
						throw new IllegalArgumentException(e.getMessage()+"/"+table1.getSimpleName());
					}
					ForeignKey foreignKey=foreignKeyField.getAnnotation(ForeignKey.class);
					if(foreignKey==null) {
						throw new IllegalArgumentException("@ForeignKey not found in "+
									domainClass.getSimpleName()+"."+foreignKeyField.getName());
					}
					Class<?> table2=foreignKey.domainClass();
					String key=id+"-"+table2.getName()+"-"+"id";
					if(join==null) {
						
						join = map.get(key);
						if(join==null) {
							join=createInnerJoin(key,table1Alias,"i"+(index++),table1, table2,id,"id");
							map.put(key, join);
						}
					}else {
						Join childJoin=getJoin(key, join.joins);
						if(childJoin==null) {
							childJoin=createInnerJoin(key,table1Alias,"i"+(index++),table1,table2,id,"id");
							join.joins.add(childJoin);
						}
						join=childJoin;
					}
					table1=table2;
					table1Alias=join.table2Alias;
				}
				innerJoinFieldAliasMap.put(field.getName(), table1Alias);
			}else {
				continue;
			}
		}
		return map;
	}
	//
	protected Join createInnerJoin(String key,String table1Alias,String table2Alias,
			Class<?> table1,Class<?> table2,String table1Field,String table2Field) {
		Join join=new Join();
		join.key=key;
		join.table1Alias=table1Alias;
		join.table2Alias=table2Alias;
		join.table1Field=table1Field;
		join.table2Field=table2Field;
		join.table1=table1;
		join.table2=table2;
		this.innerJoins.add(join);
		return join;
	}
	//
	protected QueryInfo createQueryInfo(Query query){
		List<Field> fields = ClassUtils.getFieldList(query.getClass());
		QueryDefine queryDefine = query.getClass().getAnnotation(QueryDefine.class);
		if (queryDefine == null) {
			throw new IllegalArgumentException("queryDefine not found in " + query.getClass().getName());
		}
		QueryInfo info=new QueryInfo();
		for (Field field : fields) {
			try {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				if(field.getType().equals(int.class)&&field.getName().endsWith("Sort")) {
					continue;
				}
				Class<?> fieldType = field.getType();
				Object reallyValue = field.get(query);
				if (reallyValue == null
						|| (fieldType.equals(String.class) && StringUtil.isEmpty((String) reallyValue))) {
					continue;
				}
				QueryField queryField = field.getAnnotation(QueryField.class);
				if (queryField != null && queryField.ingore()) {
					continue;
				}
				QueryFieldInfo fieldInfo=new QueryFieldInfo();
				fieldInfo.fieldType=fieldType;
				fieldInfo.value=reallyValue;
				fieldInfo.queryField=queryField;
				fieldInfo.field=field;
				if (queryField != null) {
					OrGroup orGroup=queryField.orGroup();
					if(!StringUtil.isEmpty(orGroup.group())) {
						fieldInfo.orGroup=queryField.orGroup();
					}
				}
				info.fields.add(fieldInfo);
				
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage());
			}
		}
		return info;
	}
	/**
	 * 
	 * @param q
	 */
	protected void addWheres(Query q) {
		if(q==null) {
			return;
		}
		QueryInfo queryInfo=createQueryInfo(q);
		Map<String,Object> paraMap=new HashMap<>();
		List<QueryFieldInfo> fields=queryInfo.fields;
		for (QueryFieldInfo info : fields) {
			Field field=info.field;
			try {
				Object value=field.get(q);
				paraMap.put(field.getName(), value);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				throw new SmartJdbcException(e.getMessage());
			}
		}
		for (QueryFieldInfo info : fields) {
			Field field=info.field;
			try {
				Class<?> fieldType = field.getType();
				Object value=field.get(q);
				QueryField queryField=field.getAnnotation(QueryField.class);
				String alias = MAIN_TABLE_ALIAS;
				InnerJoins innerJoins=field.getAnnotation(InnerJoins.class);
				InnerJoin innerJoin=field.getAnnotation(InnerJoin.class);
				if(innerJoin!=null||(innerJoins!=null&&innerJoins.innerJoins()!=null)||
						(queryField!=null&&!StringUtil.isEmpty(queryField.foreignKeyFields()))) {
					alias=innerJoinFieldAliasMap.get(field.getName());
				}
				//
				if(queryField!=null&&!StringUtil.isEmpty(queryField.whereSql())) {//whereSql check first
					String whereSql=queryField.whereSql();
					SqlBean sqlBean=parseSql(whereSql, paraMap);//eg:userName like #{userName}
					whereSql(sqlBean.sql,info.orGroup,sqlBean.parameters);
				}else {
					String dbFieldName=convertFieldName(field.getName());
					if(queryField!=null&&(!StringUtil.isEmpty(queryField.field()))) {
						dbFieldName=convertFieldName(queryField.field());
					}
					String operator="";
					if(queryField!=null&&(!StringUtil.isEmpty(queryField.operator()))) {
						operator=queryField.operator();
					}
					if (fieldType.equals(int[].class)||
							fieldType.equals(short[].class)||
							fieldType.equals(byte[].class)||
							fieldType.equals(String[].class)) {//in or not in
						if(fieldType.equals(int[].class)) {
							inOrNotin(alias,operator,dbFieldName, ArrayUtils.convert((int[])value));
						}else if(fieldType.equals(short[].class)) {
							inOrNotin(alias,operator,dbFieldName, ArrayUtils.convert((short[])value));
						}else if(fieldType.equals(byte[].class)) {
							inOrNotin(alias,operator, dbFieldName, ArrayUtils.convert((byte[])value));
						}else if(fieldType.equals(String[].class)) {
							inOrNotin(alias,operator, dbFieldName, ArrayUtils.convert((String[])value));
						}
						continue;
					}else if (StringUtil.isEmpty(operator)) {
						if(fieldType.equals(String.class)) {//字符串默认like
							operator="like";
						}else {
							operator="=";
						}
					}
					where(alias,dbFieldName,operator,value,info.orGroup);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				throw new IllegalArgumentException(e.getMessage());
			}
		}
	}
	public static boolean preParseSql(String sql) {
		Pattern p=Pattern.compile("\\#\\{[a-zA-Z_$][a-zA-Z0-9_$]*\\}");
		Matcher m = p.matcher(sql);
		if(m.find()) { 
		    return true;
		}
		return false;
	}
	//
	/**
	 * 
	 * @param data
	 * @param regex
	 * @return
	 */
	public static List<String> matchs(String data, String regex) {
		List<String> result = new ArrayList<>();
		if (data == null || regex == null) {
			return result;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(data);
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}
	//
	public static SqlBean parseSql(String sql,Map<String,Object> paraMap) {
		Pattern p=Pattern.compile("\\#\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
		Matcher m = p.matcher(sql);
		Pattern $p=Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
		Matcher $m = $p.matcher(sql);
		String newSql=sql;
		Object[] values=null;
		List<String> groups=new ArrayList<>();
		while(m.find()) { 
		    groups.add(m.group(1));
		}
		newSql=m.replaceAll("?");
		values=new Object[groups.size()];
		int i=0;
		for (String group : groups) {
			Object value=paraMap.get(group);
			if(value==null) {
				throw new SmartJdbcException(group+" not found.\nsql:"+sql+
						"\nall can choose paras is:"+paraMap.keySet()); 
			}
			values[i++]=value;
		}
		//
		while($m.find()) { 
			String group=$m.group(1);
			String replaceGroup="\\$\\{"+group+"\\}";
			Object value=paraMap.get(group);
			if(value==null) {
				throw new SmartJdbcException(group+" not found.\nsql:"+sql+
						"\nall can choose paras is:"+paraMap.keySet()); 
			}
			if(value instanceof String) {
				newSql=newSql.replaceAll(replaceGroup,"'"+value.toString()+"'");
			}else {
				newSql=newSql.replaceAll(replaceGroup,value.toString());
			}
		}
		return new SqlBean(newSql,values);
	}
	//
	protected int getSortFieldOrder(String[] sortFields,String fieldName) {
		if(sortFields==null||sortFields.length==0) {
			return 0;
		}
		int order=0;
		for (String field : sortFields) {
			order++;
			if(field.equals(fieldName)) {
				return order;
			}
		}
		return 0;
	}
	//
	protected void addOrderBy(Query query) {
		List<String> orderByList=addOrderByList(query);
		for (String e : orderByList) {
			orderBy(e);
		}
	}
	//
	public List<String> addOrderByList(Query query) {
		List<String> orderByList=new ArrayList<>();
		if(query==null) {
			return orderByList;
		}
		boolean haveSort=false;
		List<Field> fields = ClassUtils.getFieldList(query.getClass());
		String[] querySortFields=query.sortFields;
		List<SortField> sortFields=new ArrayList<>();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if(!field.getType().equals(int.class)) {
				continue;
			}
			String fieldName=field.getName();
			if(!fieldName.endsWith("Sort")) {
				continue;
			}
			try {
				int sortType=field.getInt(query);
				if(sortType==0) {
					continue;
				}
				String reallyFieldName=fieldName.substring(0,fieldName.length()-4);
				String dbFieldName=convertFieldName(reallyFieldName);
				SortField sortField=new SortField();
				sortField.fieldName=dbFieldName;
				sortField.sortType=sortType;
				sortField.order=getSortFieldOrder(querySortFields,fieldName);
				sortFields.add(sortField);
				haveSort=true;
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			}
		}
		sortFields.sort((a,b)->{
			return a.order-b.order;
		});
		for (SortField e : sortFields) {
			if(e.sortType==Query.SORT_TYPE_ASC) {
				orderByList.add(e.fieldName+" asc");
			}else if(e.sortType==Query.SORT_TYPE_DESC) {
				orderByList.add(e.fieldName+" desc");
			}
		}
		if(!haveSort) {
			OrderBys orderBys=query.getClass().getAnnotation(OrderBys.class);
			if(orderBys!=null&&orderBys.orderBys()!=null) {
				for (OrderBy orderBy : orderBys.orderBys()) {
					if (query.orderType != null&& query.orderType == orderBy.orderType()) {
						orderByList.add(orderBy.sql());
					}
				}
			}
			if(Config.getDefaultOrderBy()!=null) {
				Config.getDefaultOrderBy().accept(this,query);
			}
		}
		return orderByList;
	}
	//
	protected void addPaging(Query query) {
		if(query==null) {
			return;
		}
		this.limit(query.getStartPageIndex(),query.pageSize);
	}
	//
	protected void buildSelectDomainFields(){
		int index=1;
		Map<String, Join> map = new LinkedHashMap<>();
		List<Field> fields = ClassUtils.getFieldList(domainClass);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())|| Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if(includeFields!=null&&!includeFields.isEmpty()&&(!includeFields.contains(field.getName()))){
				continue;
			}
			if(excludeFields.contains(field.getName())){
				continue;
			}
			DomainField domainField = field.getAnnotation(DomainField.class);
			if(domainField!=null&&domainField.ignoreWhenSelect()) {
				continue;
			}
			if(domainField==null) {
				select(MAIN_TABLE_ALIAS, field.getName());
				continue;
			}
			boolean distinct=domainField.distinct();
			String statFunc=domainField.statFunc();
			String reallyName=field.getName();
			if(!StringUtil.isEmpty(domainField.field())) {
				reallyName=domainField.field();
			}
			//
			LeftJoin leftJoin=field.getAnnotation(LeftJoin.class);
			if(leftJoin!=null) {
				Join join=createLeftJoin(field.getName(),MAIN_TABLE_ALIAS,"l"+(index++),
						domainClass,leftJoin.table2(),leftJoin.table1Field(),leftJoin.table2Field());
				select(join.table2Alias,reallyName,null,field.getName(),distinct,statFunc);
			}else if(!StringUtil.isEmpty(domainField.foreignKeyFields())) {
				String foreignKeyId = domainField.foreignKeyFields();
				String[] foreignKeyIds=foreignKeyId.split(",");
				Class<?> table1=domainClass;
				String table1Alias=MAIN_TABLE_ALIAS;
				Join join=null;
				for (String id : foreignKeyIds) {
					Field foreignKeyField=null;
					try {
						foreignKeyField=table1.getField(id);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
						throw new IllegalArgumentException(e.getMessage()+"/"+table1.getSimpleName());
					}
					ForeignKey foreignKey=foreignKeyField.getAnnotation(ForeignKey.class);
					if(foreignKey==null) {
						throw new IllegalArgumentException("@ForeignKey not found in "+
									domainClass.getSimpleName()+"."+foreignKeyField.getName());
					}
					Class<?> table2=foreignKey.domainClass();
					String key=id;
					if(join==null) {
						join = map.get(key);
						if(join==null) {
							join=createLeftJoin(key,table1Alias,"l"+(index++),table1, table2,id);
							map.put(key, join);
						}
					}else {
						Join childJoin=getJoin(key, join.joins);
						if(childJoin==null) {
							childJoin=createLeftJoin(key,table1Alias,"l"+(index++),table1,table2,id);
							join.joins.add(childJoin);
						}
						join=childJoin;
					}
					table1=table2;
					table1Alias=join.table2Alias;
				}
				if(WRAP_TYPES.contains(field.getType())){
					addSelect(join.table2Alias, field, domainField);
				}else if(field.getGenericType() instanceof ParameterizedType){
					addSelect(join.table2Alias, field, domainField);
				}else {
					List<Field> subClassFields=getPersistentFields((Class<?>)field.getGenericType());
					for (Field subClassField : subClassFields) {
						select(join.table2Alias,subClassField.getName(),field.getName()+"_",
								subClassField.getName(),distinct,statFunc);
					}
				}
			}else {
				addSelect(MAIN_TABLE_ALIAS, field, domainField);
				continue;
			}
		}
	}
	//
	protected void addSelect(String tableAlias,Field field,DomainField domainField) {
		String selectField=field.getName();
		String asField=null;
		if(!StringUtil.isEmpty(domainField.field())) {
			asField=field.getName();
			selectField=domainField.field();
		}
		if(!StringUtil.isEmpty(domainField.statFunc())) {
			asField=field.getName();
		}
		select(tableAlias,selectField,null,asField,domainField.distinct(),domainField.statFunc());
	}
	//
	protected String getSinglePrimaryKey(Class<?> clazz) {
		List<Field> list=SqlProvider.getPrimaryKey(clazz);
		if(list.size()>1||list.size()==0) {
			throw new SmartJdbcException("PrimaryKey column can only be one");
		}
		return list.get(0).getName();
	}
	//
	protected Join getJoin(String key,List<Join> list) {
		for (Join join : list) {
			if(join.key.equals(key)) {
				return join;
			}
		}
		return null;
	}
	//
	protected Join createLeftJoin(String key,String table1Alias,String table2Alias,Class<?> table1,Class<?> table2,
			String table1Field) {
		return createLeftJoin(key, table1Alias, table2Alias, table1, table2, table1Field, getSinglePrimaryKey(table2));
	}
	//
	protected Join createLeftJoin(String key,String table1Alias,String table2Alias,Class<?> table1,Class<?> table2,
			String table1Field,String table2Field) {
		Join join = new Join();
		join.key=key;
		join.table1Alias=table1Alias;
		join.table2Alias=table2Alias;
		join.table1=table1;
		join.table2 = table2;
		join.table1Field=table1Field;
		join.table2Field= table2Field;
		leftJoins.add(join);
		return join;
	}
	
	/**
	 * 
	 * @return
	 */
	protected SqlBean queryCount() {
		StringBuffer sql = new StringBuffer();
		sql.append("\nselect count(1) \n");
		this.needPaging=false;
		return build(sql);
	}
	
	/**
	 * 
	 * @return
	 */
	protected SqlBean query() {
		StringBuffer sql = new StringBuffer();
		if(!ingoreSelectDomainFiled) {
			buildSelectDomainFields();
		}
		sql.append("\nselect ");
		if(selectFields.size()==0) {
			throw new IllegalArgumentException("no select field found in "+domainClass.getName());
		}
		addSelectFields(sql);
		return build(sql);
	}
	
	protected void addSelectFields(StringBuffer sql) {
		for (SelectField field : selectFields) {
			if(field.distinct) {
				sql.append(" distinct ");
			}
			if(StringUtil.isEmpty(field.statFunction)) {
				sql.append(field.tableAlias).append(".`");
				sql.append(convertFieldName(field.field)).append("`");
			}else {
				sql.append(field.statFunction);
				sql.append("(");
				sql.append(field.tableAlias).append(".`");
				sql.append(convertFieldName(field.field)).append("`");
				sql.append(")");
			}
			if(field.asField!=null) {
				String asField=convertFieldName(field.asField);
				if(field.preAsField!=null) {
					asField=field.preAsField+asField;
				}
				sql.append(" as `").append(asField).append("`");
			}
			sql.append(",");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append("\n");
	}
	//
	//
	protected String getFromSql() {
		StringBuffer sql=new StringBuffer();
		sql.append("from ").append(getTableName(domainClass)).append(" ").append(MAIN_TABLE_ALIAS).append(" \n");
		//inner join
		this.innerJoinMap=getInnerJoins(query);
		for (Join join : innerJoins) {
			sql.append("inner join  ");
			sql.append(getTableName(join.table2)).append(" ").append(join.table2Alias);
			sql.append(" on ").append(join.table1Alias).append(".`"+convertFieldName(join.table1Field)+"`=").
				append(join.table2Alias).append(".").append(convertFieldName(join.table2Field));
			sql.append("\n");
		}
		//left join
		for (Join join : leftJoins) {
			sql.append("left join  ");
			sql.append(getTableName(join.table2)).append(" ").append(join.table2Alias);
			sql.append(" on ").append(join.table1Alias).append(".`"+convertFieldName(join.table1Field)+"`=").
				append(join.table2Alias).append(".").append(convertFieldName(join.table2Field));
			sql.append("\n");
		}
		return sql.toString();
	}
	//
	protected String getWhereSql() {
		StringBuffer sql=new StringBuffer();
		addWheres(query);
		sql.append("where 1=1 ");
		for (Where w : qw.getWheres()) {
			if(w.alias==null) {
				w.alias=MAIN_TABLE_ALIAS;
			}
		}
		sql.append(qw.whereStatement().sql);
		sql.append("\n");
		return sql.toString();
	}
	//
	protected String getGroupBySql() {
		StringBuffer sql=new StringBuffer();
		if(groupBys.size()>0) {
			sql.append("group by ");
			for (GroupByField field : groupBys) {
				if(!StringUtil.isEmpty(field.tableAlias)) {
					sql.append(field.tableAlias).append(".");
				}
				sql.append(convertFieldName(field.field)).append(",");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append("\n");
		}
		return sql.toString();
	}
	//
	protected String getOrderBySql() {
		if(isSelectCount) {
			return "";
		}
		StringBuffer sql=new StringBuffer();
		if(needOrderBy) {
			addOrderBy(query);
			if (qw.getOrderBys().size()>0) {
				sql.append("order by ");
				for (String orderBy : qw.getOrderBys()) {
					sql.append(orderBy).append(",");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append("\n");
			}
		}
		return sql.toString();
	}
	//
	protected String getLimitSql() {
		if(isSelectCount) {
			return "";
		}
		StringBuffer sql=new StringBuffer();
		addPaging(query);	
		if(qw.getLimitEnd()!=-1) {
			sql.append("limit ").append(qw.getLimitStart()).append(",").append(qw.getLimitEnd()).append("\n");
		}
		return sql.toString();
	}
	//
	protected String getForUpdateSql() {
		if(isForUpdate) {
			return "for update \n";
		}
		return "";
	}
	//
	protected SqlBean build(StringBuffer selectSql) {
		SqlBean bean=new SqlBean();
		bean.selectSql=selectSql.toString();
		bean.fromSql=getFromSql();
		bean.whereSql=getWhereSql();
		bean.groupBySql=getGroupBySql();
		bean.orderBySql=getOrderBySql();
		bean.limitSql=getLimitSql();
		bean.forUpdateSql=getForUpdateSql();
		bean.sql=bean.toSql();
		bean.parameters=qw.whereValues();
		return bean;
	}
		
	public Class<?> getDomainClass() {
		return domainClass;
	}

	@Override
	public SqlBean build() {
		if(isSelectCount) {
			return queryCount();
		}
		return query();
	}
	
	public List<SelectField> getSelectFields() {
		return selectFields;
	}
	
	public void setSelectFields(List<SelectField> selectFields) {
		this.selectFields = selectFields;
	}
}
