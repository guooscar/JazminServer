/**
 * 
 */
package jazmin.test.server.mysqlproxy;

import jazmin.util.JdbcUtil;

/**
 * @author yama
 *
 */
public class TestLocalConnect {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		String url="jdbc:mysql://localhost:5050/db_webb?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true";
		JdbcUtil.executeQuery(url, "yan", "123", "select 1", (a,b)->{
			
		});
	}

}
