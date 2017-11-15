/**
 * 
 */
package jazmin.driver.jdbc.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author yama
 *
 */
public class Query {
	public static enum OprType{
		eq,
		like,
		gt,
		lt,
		neq,
		in,
		nin,
	}
	//
	@Target(ElementType.FIELD)  
	@Inherited  
	public static @interface Opr {
		OprType value();
	}
	//
	public int pageSize;
	public int pageIndex;
	//
	/**
	 * 排除的字段
	 */
	Set<String> excludes;
	/**
	 * 查询的字段
	 * 查询字段的优先级高于排除字段，如果查询字段设置了，则不使用排除字段
	 */
	Set<String>includes;
	//
	@Opr(OprType.eq)
	public Integer id;
	//
	public String sql;
	//
	public Query include(String ...fields){
		if(includes==null){
			includes=new TreeSet<>();
		}
		for(String s :fields){
			includes.add(s);
		}
		return this;
	}
	//
	public Query exclude(String ...fields){
		if(excludes==null){
			excludes=new TreeSet<>();
		}
		for(String s :fields){
			excludes.add(s);
		}
		return this;
	}
	//
	public static Query id(int id){
		Query q=new Query();
		q.id=id;
		return q;
	}
}
