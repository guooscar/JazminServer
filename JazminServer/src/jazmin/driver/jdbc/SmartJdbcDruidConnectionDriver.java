/**
 * 
 */
package jazmin.driver.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import io.itit.smartjdbc.Config;
import io.itit.smartjdbc.connection.TransactionManager;
import jazmin.driver.jdbc.DruidConnectionDriver;

/**
 * 
 * @author skydu
 *
 */
public class SmartJdbcDruidConnectionDriver extends DruidConnectionDriver 
implements TransactionManager{
	//
	public SmartJdbcDruidConnectionDriver() throws SQLException {
		super();
		Config.setTransactionManager(this);
	}

	//
	@Override
	public Connection getConnecton(String datasourceIndex) {
		return getConnection();
	}

}
