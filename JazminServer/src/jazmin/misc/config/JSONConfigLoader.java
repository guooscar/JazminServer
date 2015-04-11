/**
 * 
 */
package jazmin.misc.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class JSONConfigLoader {
	private static Logger logger=LoggerFactory.get(JSONConfigLoader.class);
	//
	private Map<String,Map<String,Object>>allConfigMap;
	public JSONConfigLoader() {
		allConfigMap=new ConcurrentHashMap<>();
	}
	//
	public void clear(){
		allConfigMap.clear();
	}
	//
	@SuppressWarnings("unchecked")
	public <T> Map<String,T> clear(Class<T>clazz){
		return (Map<String, T>) allConfigMap.remove(clazz.getSimpleName());
	}
	//
	public void loadConfig(Class<?>clazz,String content){
		logger.info("load config for class:"+clazz.getName());
		Map<String,Object>config=fromJsonMap(content,clazz);
		logger.info("loaded config for class:"+clazz.getName()+" size:"+config.size());
		allConfigMap.put(clazz.getSimpleName(),config);
	}
	//
	//
	@SuppressWarnings("unchecked")
	public <T> T getConfig(Class<T>clazz,Object ...key){
		Map<String,Object>configMap=allConfigMap.get(clazz.getSimpleName());
		StringBuilder strKey=new StringBuilder();
		for(Object o : key){
			strKey.append(o+"-");
		}
		strKey.deleteCharAt(strKey.length()-1);
		String realKey=strKey.toString();
		if(configMap==null||!configMap.containsKey(realKey)){
			throw new IllegalArgumentException("can not find config:"+
									clazz.getSimpleName()+"#"+realKey);
		}
		return (T) configMap.get(realKey);
	}
	//
	@SuppressWarnings("unchecked")
	public <T>Map<String,T>getConfigMap(Class<T>clazz){
		Map<String,Object>configMap=allConfigMap.get(clazz.getSimpleName());
		if(configMap==null){
			throw new IllegalArgumentException("can not find config:"+
									clazz.getSimpleName());
		}
		return (Map<String, T>) configMap;
	}
	//
	@SuppressWarnings("unchecked")
	public <T>List<T>getConfigList(Class<T>clazz){
		Map<String,Object>configMap=allConfigMap.get(clazz.getSimpleName());
		if(configMap==null){
			throw new IllegalArgumentException("can not find config:"+
									clazz.getSimpleName());
		}
		return (List<T>) new ArrayList<>(configMap.values());
	}
	//
	public  int getConfigSize(Class<?>clazz){
		Map<String,Object>configMap=allConfigMap.get(clazz.getSimpleName());
		if(configMap==null){
			throw new IllegalArgumentException("can not find config:"+
									clazz.getSimpleName());
		}
		return configMap.size();
	}
	//
	//
	@SuppressWarnings("unchecked")
	public static <T> Map<String,T> fromJsonMap(String str,Class<?>t){
		List<T> l= (List<T>) JSON.parseArray(str, t);	
		Map<String,T>m=new HashMap<String, T>();
		List<Field> mainFields=new ArrayList<>();
		for(Field f:t.getFields()){
			if(f.isAnnotationPresent(JSONConfigPrimaryKey.class)){
				mainFields.add(f);
			}
		}
		if(mainFields.isEmpty()){
			throw new IllegalArgumentException("can not find JSONPrimaryKey "
					+ "Annotation on class :"+t);
		}
		Collections.sort(mainFields,(o1, o2)->{
			JSONConfigPrimaryKey p1=o1.getAnnotation(JSONConfigPrimaryKey.class);
			JSONConfigPrimaryKey p2=o1.getAnnotation(JSONConfigPrimaryKey.class);
			return p1.order()-p2.order();
		});
		for(T tt:l){
			StringBuilder key=new StringBuilder();
			for(Field mainField:mainFields){
				Object oo=getFieldValue(mainField,tt);
				if(oo==null){
					throw new IllegalArgumentException("primary key :"+
									mainField.getName()+" should not null.");
				}
				key.append(oo).append("-");
			}
			key.deleteCharAt(key.length()-1);
			//
			//check field value
			for(Field allField:t.getFields()){
				checkFieldValue(allField,tt);
			}
			//
			m.put(key.toString(),tt);
		}
		return m;
	}
	//
	private static void checkFieldValue(Field field,Object o){
		Object oo=getFieldValue(field, o);
		JSONConfigField fieldAnn=field.getAnnotation(JSONConfigField.class);
		if(fieldAnn!=null){
			if(fieldAnn.notNull()&&oo==null){
				throw new IllegalArgumentException("field:"+field.getName()
						+" should not null.");
			}
			if(oo instanceof Number){
				Number nn=(Number)oo;
				if(nn.intValue()<fieldAnn.min()){
					throw new IllegalArgumentException("field:"+field.getName()
							+" shoule >="+fieldAnn.min());
				}
				if(nn.intValue()>fieldAnn.max()){
					throw new IllegalArgumentException("field:"+field.getName()
							+" shoule <="+fieldAnn.max());
				}
			}
			//
			if(field.getType().isArray()){
				int arrayLength=Array.getLength(oo);
				if(fieldAnn.size()!=0&&fieldAnn.size()!=arrayLength){
					throw new IllegalArgumentException("field:"+field.getName()
							+" length should = "+fieldAnn.size());
				}
			}
			//
			if(oo instanceof List){
				List<?> lll=(List<?>)oo;
				int listSize=lll.size();
				if(fieldAnn.size()!=0&&fieldAnn.size()!=listSize){
					throw new IllegalArgumentException("field:"+field.getName()
							+" length should = "+fieldAnn.size());
				}
			}
		}
	}
	//
	private static Object getFieldValue(Field field,Object o){
		field.setAccessible(true);
		Object oo=null;
		try{
			oo=field.get(o);
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
		return oo;
	}
}
