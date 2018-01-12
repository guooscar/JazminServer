package jazmin.test.driver.jdbc;

import java.util.List;

import jazmin.driver.jdbc.smartjdbc.Query;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainDefine;
import jazmin.driver.jdbc.smartjdbc.annotations.DomainField;
import jazmin.driver.jdbc.smartjdbc.annotations.ForeignKey;
import jazmin.driver.jdbc.smartjdbc.annotations.InnerJoin;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryDefine;
import jazmin.driver.jdbc.smartjdbc.annotations.QueryField;

/**
 * 
 * @author skydu
 *
 */
public class SmartDAOTest {
	//
	public static class Department{
		public int id;
		public String name;
	}
	
	public static class User{
		public int id;
		public String name;
		@ForeignKey(domainClass=Department.class)
		public int departmentId;
	}
	
	@DomainDefine(domainClass=Team.class)
	public static class Team{
		public int id;
		public String name;
		public int money;
		public int point;
		@ForeignKey(domainClass=User.class)
		public int createUserId;
		@ForeignKey(domainClass=User.class)
		public int updateUserId;
		public List<String> grades;
	}
	
	public static class TeamInfo extends Team{
		//
		@DomainField(foreignKeyFields="createUserId",field="name")
		public String createUserName;
		
		@DomainField(foreignKeyFields="updateUserId",field="name")
		public String updateUserName;
		
		@DomainField(foreignKeyFields="createUserId,departmentId",field="name")
		public String createUserDepartmentName;
		
		@DomainField(foreignKeyFields="updateUserId,departmentId",field="name")
		public String updateUserDepartmentName;
	}
	
	public static class TeamDetailInfo extends Team{
		//
		@DomainField(foreignKeyFields="createUserId")
		public User createUser;
		
		@DomainField(foreignKeyFields="updateUserId")
		public User updateUser;
		
		@DomainField(foreignKeyFields="createUserId,departmentId")
		public Department createUserDepartment;
	}
	
	public static class TeamStat{
		public int id;
		public String name;
		@DomainField(statFunc="sum",field="money")
		public long totalMoney;
		@DomainField(statFunc="sum",field="point")
		public long totalPoint;
	}
	
	@QueryDefine(domainClass=Team.class)
	public static class TeamQuery extends BizQuery{
		public String name;
		
		@InnerJoin(table1Field="createUserId",table2=User.class)
		@QueryField(field="name")
		public String createUserName;
	}
	
	public static void main(String[] args) {
		Query.defaultOrderType=BizQuery.ORDER_TYPE_CREATE_TIME_ASC;
		TeamQuery query=new TeamQuery();
		query.name="skydu";
		query.createUserName="royi";
		new BizSelectProvider(TeamInfo.class).selectCount().query(query).needOrderBy(false).build();
		new BizSelectProvider(TeamInfo.class).query(query).build();
		new BizSelectProvider(TeamStat.class).query(query).groupBy("createUserId").build();
		new BizSelectProvider(TeamDetailInfo.class).query(query).build();
	}
}
