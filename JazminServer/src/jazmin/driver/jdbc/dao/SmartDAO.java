/**
 * 
 */
package jazmin.driver.jdbc.dao;

import java.util.List;

/**
 * @author yama
 *
 */
public class SmartDAO {
	//插入对象，更新主键字段的值
	public void insert(Object object){
		//
	}
	//
	//按照主键更新，如果domain类中无法找到主键，则抛出异常
	//
	public int update(Object object){
		return 0;
	}
	//按照query指定的字段更新,返回更新的行数
	public int update(Object object,Query query){
		return 0;
	}
	
	
	//按照query 删除，返回删除的行数
	public int delete(Class<?>domainClass,Query query){
		return 0;
	}
	//
	//查询列表，自动的关联外键，可以指定排除字段
	public <T> List<T> queryList(Class<?>domainClass,Query query){
		return null;
	}
	//
	public <T> T queryObject(Class<?>domainClass,Query query){
		return null;
	}
	
	//
	public int queryCount(Class<?>domainClass,Query query){
		return 0;
	}
}
