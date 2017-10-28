/**
 * 
 */
package jazmin.core.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.core.Registerable;
import jazmin.core.Server;
import jazmin.core.app.AutoWiredObject.AutoWiredField;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
/**
 * 
 * @author yama
 * 1 Apr, 2015
 */
public class Application extends Lifecycle {
	private static Logger logger=LoggerFactory.get(Application.class);
	//
	private Map<Class<?>,AutoWiredObject>autoWiredMap=
			new ConcurrentHashMap<Class<?>, AutoWiredObject>();
	//
	private boolean autoRegisterWired;
	
	/**
	 * @return the autoRegisterWired
	 */
	public boolean isAutoRegisterWired() {
		return autoRegisterWired;
	}
	/**
	 * @param autoRegisterWired the autoRegisterWired to set
	 */
	public void setAutoRegisterWired(boolean autoRegisterWired) {
		this.autoRegisterWired = autoRegisterWired;
	}
	//
	@Override
	public String info() {
		return "Application class:"+getClass().getName();
	}
	/**
	 * get auto wired object
	 * @param clazz the auto wired object class 
	 * @return the auto wired object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getWired(Class<T>clazz){
		AutoWiredObject ao=autoWiredMap.get(clazz);
		if(ao==null){
			return null;
		}
		T instance =(T) ao.instance;
		return instance;
	} 
	//
	public void wiredApplicationAndregister() throws Exception{
		createWired(this);
		//
		for(Lifecycle lc:Jazmin.getLifecycles()){
			for(AutoWiredObject obj :getAutoWiredObjects()){
				if(lc instanceof Registerable){
					((Registerable) lc).register(obj.instance);
				}
			}	
		}
	}
	//
	@SuppressWarnings("unchecked")
	public void createWired(Object instance)throws Exception{
		AutoWiredObject autoWiredObject=new AutoWiredObject();
		Class<?> clazz=instance.getClass();
		autoWiredObject.clazz=clazz;
		autoWiredObject.instance=instance;
		autoWiredMap.put(clazz,autoWiredObject);
		if(logger.isDebugEnabled()){
			logger.debug("create wired object {}",clazz.getName());
		}
		//
		//
		for(Field f:getField(clazz)){
			if(f.isAnnotationPresent(AutoWired.class)){
				AutoWired aw=f.getAnnotation(AutoWired.class);
				AutoWiredField af=new AutoWiredField(f.getName(),f.getType(),aw.shared());
				autoWiredObject.fields.add(af);
				f.setAccessible(true);
				Class<?>fieldType=f.getType();
				if(Driver.class.isAssignableFrom(fieldType)){
					Object target=Jazmin.getDriver(
							(Class<? extends Driver>) fieldType);
					af.hasValue=target!=null;
					if(target!=null){
						f.set(instance,target);		
					}else{
						logger.warn("can not find autowired driver:"+fieldType);
					}
				}else if(Server.class.isAssignableFrom(fieldType)){
					Object target=Jazmin.getServer(
							(Class<? extends Server>) fieldType);
					af.hasValue=target!=null;
					if(target!=null){
						f.set(instance,target);		
					}else{
						logger.warn("can not find autowired server:"+fieldType);
					}
				}else{
					af.hasValue=true;
					//shared auto wire property 
					if(aw.shared()){
						AutoWiredObject ao=autoWiredMap.get(fieldType);
						Object target=(ao==null?null:ao.instance);
						if(target==null){
							target=createWired(fieldType);
						}
						f.set(instance,target);	
	
					}else{
						Object target=createWired(fieldType);
						f.set(instance,target);	
					}
				}
			}
		}
		//
		for(Method f:clazz.getMethods()){
			if(f.isAnnotationPresent(AutoWireCompleted.class)){
				if(f.getParameterTypes().length!=0){
					logger.warn("AutoWireCompleted method:{} must be zero args",f);
					continue;
				}
				if(logger.isDebugEnabled()){
					logger.debug("invoke AutoWireCompleted method:{}",f);
				}
				f.invoke(instance);
			}
		}
	}
	// 
	/**
	 * create auto wired object
	 * @param clazz the auto wired object class
	 * @return the auto wired object
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public <T> T createWired(Class<T>clazz)throws Exception{
		if(autoWiredMap.containsKey(clazz)){
			return (T) autoWiredMap.get(clazz).instance;
		}
		try{
			T instance =clazz.newInstance();
			createWired(instance);
			return instance;
		}catch (Exception e) {
			logger.fatal("can not create wired object of class "+clazz);
			throw e;
		}
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
	//
	private void getParentField(Class<?>clazz,Set<Field>fields){
		Class<?>superClass=clazz.getSuperclass();
		if(clazz.getSuperclass()==null){
			return;
		}
		fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
		getParentField(superClass, fields);
	}
	//
	public List<AutoWiredObject>getAutoWiredObjects(){
		return new ArrayList<>(autoWiredMap.values());
	}
}
