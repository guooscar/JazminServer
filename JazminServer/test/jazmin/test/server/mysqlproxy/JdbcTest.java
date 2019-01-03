/**
 * 
 */
package jazmin.test.server.mysqlproxy;

import jazmin.util.JdbcUtil;

/**
 * @author yama
 *
 */
public class JdbcTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		JdbcUtil.execute("jdbc:mysql://localhost:5050/db_amjy?useUnicode=true&characterEncoding=UTF-8","yan","123", "select 1");
	}

}
