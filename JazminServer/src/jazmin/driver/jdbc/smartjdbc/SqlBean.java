package jazmin.driver.jdbc.smartjdbc;

/**
 * 
 * @author skydu
 *
 */
public class SqlBean {

	public String sql;
	
	public Object[] parameters;
	
	public String selectSql;//select a.*,b.name ....
	
	public String fromSql;//from t_bug a inner join t_user i0
	
	public String whereSql;//where ...
	
	public String groupBySql;//group by ...
	
	public String orderBySql;//order by ...

	public String limitSql;//limit 0,10
	
	public String forUpdateSql;//for update
	//
	public SqlBean() {
		
	}
	//
	public SqlBean(String sql, Object[] parameters) {
		this.sql=sql;
		this.parameters=parameters;
	}
	//
	public SqlBean(String sql) {
		this(sql,new Object[0]);
	}
	//
	public String toSql() {
		StringBuffer sql=new StringBuffer();
		sql.append(selectSql);
		sql.append(fromSql);
		sql.append(whereSql);
		sql.append(groupBySql);
		sql.append(orderBySql);
		sql.append(limitSql);
		sql.append(forUpdateSql);
		return sql.toString();
	}
}
