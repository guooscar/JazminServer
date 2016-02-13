package jazmin.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import jazmin.core.Jazmin;

import org.josql.Query;
import org.josql.QueryResults;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class BeanUtil {
	//
	public static <T> List<T> copyFromList(Class<T>toClass,List<?>fromBeanList)
			throws Exception{
		List<T>result=new ArrayList<>();
		for(Object o:fromBeanList){
			result.add(copyFrom(toClass,o));
		}
		return result;
	}
	//
	public static  <T> T copyFrom(Class<T>toClass,Object from)throws Exception{
		T to=null;
		to =  toClass.newInstance();
		Class<?>beanClass=from.getClass();
		Field ff[]=toClass.getDeclaredFields();
		for(Field f:ff){
			if(Modifier.isFinal(f.getModifiers())){
				continue;
			}
			try{
				Field bf=beanClass.getDeclaredField(f.getName());
				bf.setAccessible(true);
				Object fv=bf.get(from);
				f.setAccessible(true);
				f.set(to,fv);
			}catch (Exception e) {}
		}
		return to;
	}
	//
	public static <T> List<T> copyToList(Class<T>toClass,List<?>fromBeanList)
			throws Exception{
		List<T>result=new ArrayList<>();
		for(Object o:fromBeanList){
			result.add(copyTo(toClass,o));
		}
		return result;
	}
	//
	public static  <T> T copyTo(Class<T>toClass,Object from)throws Exception{
		T to=null;
		to =  toClass.newInstance();
		Field ff[]=from.getClass().getDeclaredFields();
		for(Field f:ff){
			if(Modifier.isFinal(f.getModifiers())){
				continue;
			}
			try{
				Field toField=toClass.getDeclaredField(f.getName());
				f.setAccessible(true);
				Object fv=f.get(from);
				toField.setAccessible(true);
				toField.set(to,fv);
			}catch (Exception e) {}
		}
		return to;
	}
	//
	@SuppressWarnings("unchecked")
	public static <T>List<T>query(List<T>collection,String sql){
		try{
			Query query=new Query();
			query.setClassLoader(Jazmin.getAppClassLoader());
			query.parse(sql);
			QueryResults qr = query.execute (collection);
			return qr.getResults();
		}catch(Throwable e){
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
