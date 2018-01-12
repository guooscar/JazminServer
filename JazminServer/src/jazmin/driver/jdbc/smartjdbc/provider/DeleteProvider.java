package jazmin.driver.jdbc.smartjdbc.provider;

import jazmin.driver.jdbc.smartjdbc.QueryWhere;
import jazmin.driver.jdbc.smartjdbc.SqlBean;

/**
 * 
 * @author skydu
 *
 */
public class DeleteProvider extends SqlProvider{
	//
	Class<?> domainClass;
	QueryWhere qw;
	//
	public DeleteProvider(Class<?> domainClass,QueryWhere qw) {
		this.domainClass=domainClass;
		this.qw=qw;
	}
	
	@Override
	public SqlBean build() {
		StringBuffer sql=new StringBuffer();
		String tableName=getTableName(domainClass);
		sql.append("delete from ").append(tableName);
		sql.append(" where 1=1");
		sql.append(qw.whereStatement());
		return createSqlBean(sql.toString(),qw.whereValues());
	}

}
