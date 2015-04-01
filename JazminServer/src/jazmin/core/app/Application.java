/**
 * 
 */
package jazmin.core.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class Application extends Lifecycle {
	private static Logger logger=LoggerFactory.get(Application.class);
	//
	private Map<Class<?>,Object>autoWiredMap=new HashMap<Class<?>, Object>();
	//
	@Override
	public String info() {
		return "Application class:"+getClass().getName();
	}
	//
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T>clazz)throws Exception{
		T instance =clazz.newInstance();
		for(Field f:getField(clazz)){
			System.err.println(f);
			if(f.isAnnotationPresent(AutoWired.class)){
				f.setAccessible(true);
				Class<?>fieldType=f.getType();
				if(Driver.class.isAssignableFrom(fieldType)){
					Object target=Jazmin.driver(
							(Class<? extends Driver>) fieldType);
					if(target!=null){
						f.set(instance,target);		
					}else{
						logger.warn("can not find autowired driver:"+fieldType);
					}
				}else if(Server.class.isAssignableFrom(fieldType)){
					Object target=Jazmin.server(
							(Class<? extends Server>) fieldType);
					if(target!=null){
						f.set(instance,target);		
					}else{
						logger.warn("can not find autowired server:"+fieldType);
					}
				}else{
					System.err.println("set up :"+fieldType);
					Object target=autoWiredMap.get(fieldType);
					if(target==null){
						target=create(fieldType);
						autoWiredMap.put(fieldType,target);
					}
					f.set(instance,target);	
				}
			}
		}
		return instance;
	}
	//
	private Set<Field> getField(Class<?>clazz){
		HashSet<Field>result=new HashSet<Field>();
		result.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if(clazz.getSuperclass()!=null){
			getParentField(clazz,result);
		}
		return result;
	}
	
	private void getParentField(Class<?>clazz,Set<Field>fields){
		Class<?>superClass=clazz.getSuperclass();
		if(clazz.getSuperclass()==null){
			return;
		}
		fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
		getParentField(superClass, fields);
	}
}
