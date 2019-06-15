/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author yama
 * @date Jun 5, 2014
 */
public class JSONUtil {
	public static interface JSONPropertyFilter{
		boolean apply(Object object, String name, Object arg2);
	}
	/**
	 *convert object to json string 
	 */
	public static String toJson(Object obj,
			JSONPropertyFilter propertyFilter,
			boolean prettyFormat){
		if(prettyFormat){
			return JSON.toJSONString(obj,new FastJsonPropertyFilter(propertyFilter),
					SerializerFeature.PrettyFormat);
		}else{
			return JSON.toJSONString(obj,new FastJsonPropertyFilter(propertyFilter));	
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
		return JSON.toJSONString(obj);
	}
	/**
	 * convert json string to class
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String str,Class<?>t){
		return (T) JSON.parseObject(str, t);
	}
	/**
	 *convert json to class list 
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromJsonList(String str,Class<?>t){
		return  (List<T>) JSON.parseArray(str, t);	
	}
	
	/**
	 * convert json string to Map
	 * @param json
	 * @param keyType
	 * @param valueType
	 * @return
	 */
	public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType,  Class<V> valueType) {
	     return JSON.parseObject(json,new TypeReference<Map<K, V>>(keyType, valueType) {});
	}
	
	/**
	 * 
	 * @param json
	 * @param t
	 * @return
	 */
	public static <T> Set<T> fromJsonSet(String json,Class<T>t){
		return  JSON.parseObject(json,new TypeReference<LinkedHashSet<T>>(){});
	}
	
	/**
	 * 
	 * @param json
	 * @param t
	 * @return
	 */
	public static <T> Set<T> fromJsonLinkedHashSet(String json,Class<T>t){
		return  JSON.parseObject(json,new TypeReference<LinkedHashSet<T>>(){});
	}
}
