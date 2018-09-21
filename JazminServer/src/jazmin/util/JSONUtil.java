/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * @date Jun 5, 2014
 */
public class JSONUtil {
	//
	private static Logger logger=LoggerFactory.get(JSONUtil.class);
	//
	public static interface JSONPropertyFilter{
		boolean apply(Object object, String name, Object arg2);
	}
	/**
	 *convert object to json string 
	 */
	public static String toJson(Object obj,
			JSONPropertyFilter propertyFilter,
			boolean prettyFormat){
		try {
			if(prettyFormat){
				return JSON.toJSONString(obj,new FastJsonPropertyFilter(propertyFilter),
						SerializerFeature.PrettyFormat);
			}else{
				return JSON.toJSONString(obj,new FastJsonPropertyFilter(propertyFilter));	
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
		
	}
	//
	private static class FastJsonPropertyFilter implements PropertyFilter{
		JSONPropertyFilter propertyFilter;
		public FastJsonPropertyFilter(JSONPropertyFilter filter) {
			this.propertyFilter=filter;
		}
		@Override
		public boolean apply(Object arg0, String arg1, Object arg2) {
			return propertyFilter.apply(arg0, arg1, arg2);
		}
	} 
	/**
	 *convert object to json string 
	 */
	public static String toJson(Object obj){
		try {
			return JSON.toJSONString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
		
	}
	/**
	 * convert json string to class
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String str,Class<?>t){
		try {
			return (T) JSON.parseObject(str, t);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
	}
	/**
	 *convert json to class list 
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromJsonList(String str,Class<?>t){
		try {
			return  (List<T>) JSON.parseArray(str, t);	
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
	}
	
	/**
	 * convert json string to Map
	 * @param json
	 * @param keyType
	 * @param valueType
	 * @return
	 */
	public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType,  Class<V> valueType) {
		try {
			return JSON.parseObject(json,new TypeReference<Map<K, V>>(keyType, valueType) {});
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
	     
	}
	
	/**
	 * 
	 * @param json
	 * @param t
	 * @return
	 */
	public static <T> Set<T> fromJsonSet(String json,Class<T>t){
		try {
			return  JSON.parseObject(json,new TypeReference<Set<T>>(){});
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AppException(e.getMessage());
		}
	}
	
}
