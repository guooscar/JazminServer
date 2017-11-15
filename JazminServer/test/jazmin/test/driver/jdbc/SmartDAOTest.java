/**
 * 
 */
package jazmin.test.driver.jdbc;

import jazmin.driver.jdbc.dao.ForeignKey;
import jazmin.driver.jdbc.dao.PrimaryKey;

/**
 * @author yama
 *
 */

public class SmartDAOTest {

	//
	@ForeignKey(table=User.class,prefix="createUser")
	@ForeignKey(table=User.class,prefix="ownerUser",on="userId")
	
	public static class Domain{
		public int id;
		public String name;
		//
		public int createUserId;
		public int createUserName;
		//
		public int updateUserId;
		public User updateUser;
		//
		public int ownerUserId;
		public String ownerUserLogo;
		public String ownerUserName;
	}
	//
	public static class User{
		@PrimaryKey
		public int userId;
		//
		public String name;
	}
	//
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
