package jazmin.driver.jdbc.smartjdbc.provider;

import jazmin.driver.jdbc.smartjdbc.QueryWhere;
import jazmin.driver.jdbc.smartjdbc.QueryWhere.WhereStatment;
import jazmin.driver.jdbc.smartjdbc.SqlBean;

/**
 * 
 * @author skydu
 *
 */
public class DeleteProvider extends SqlProvider{
	//
	protected Class<?> domainClass;
	protected QueryWhere qw;
	//
	public DeleteProvider(Class<?> domainClass,QueryWhere qw) {
		this.domainClass=domainClass;
		this.qw=qw;
	}
	
	@Override
	public SqlBean build() {
		StringBuffer sql=new StringBuffer();
		String tableName=getTableName(domainClass);
		sql.append("delete from ").append(tableName).append(" ");
		sql.append("where 1=1 ");
		WhereStatment ws=qw.whereStatement();
		sql.append(ws.sql);
		return createSqlBean(sql.toString(),ws.values);
	}

}
