/**
 * 
 */
package jazmin.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.misc.InfoBuilder;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class Environment extends Lifecycle{
	private Map<String,String>map;
	public Environment() {
		map=new HashMap<String, String>();
	}
	/**
	 * put key/value to environment.
	 */
	public void put(String k,String v){
		if(isStarted()){
			throw new IllegalStateException("put before started");
		}
		map.put(k, v);
	}
	/**
	 * @return string value by key
	 */
	public String getString(String key,String defaultValue){
		String v=map.get(key);
		return v==null?defaultValue:v;
	}
	/**
	 * @return string value by key
	 */
	public String getString(String key){
		return map.get(key);
	}
	/**
	 * @return integer value by key
	 */
	public Integer getInteger(String key){
		String v=map.get(key);
		if(v==null){
			return null;
		}
		return Integer.valueOf(v);
	}
	/**
	 * @return integer value by key
	 */
	public Integer getInteger(String key,Integer defaultValue){
		String v=map.get(key);
		if(v==null){
			return defaultValue;
		}
		return Integer.valueOf(v);
	}
	/**
	 * @return boolean value by key
	 */
	public Boolean getBoolean(String key,Boolean defaultValue){
		String v=map.get(key);
		if(v==null){
			return defaultValue;
		}
		return Boolean.valueOf(v);
	}
	/**
	 * @return boolean value by key
	 */
	public Boolean getBoolean(String key){
		String v=map.get(key);
		if(v==null){
			return null;
		}
		return Boolean.valueOf(v);
	}
	/**
	 * @return float value by key
	 */
	public Float getFloat(String key){
		String v=map.get(key);
		if(v==null){
			return null;
		}
		return Float.valueOf(v);
	}
	/**
	 * @return float value by key
	 */
	public Float getFloat(String key,Float defaultValue){
		String v=map.get(key);
		if(v==null){
			return defaultValue;
		}
		return Float.valueOf(v);
	}
	/**
	 * @return double value by key
	 */
	public Double getDouble(String key,Double defaultValue){
		String v=map.get(key);
		if(v==null){
			return defaultValue;
		}
		return Double.valueOf(v);
	}
	/**
	 * @return double value by key
	 */
	public Double getDouble(String key){
		String v=map.get(key);
		if(v==null){
			return null;
		}
		return Double.valueOf(v);
	}
	/**
	 * @return long value by key
	 */
	public Long getLong(String key){
		String v=map.get(key);
		if(v==null){
			return null;
		}
		return Long.valueOf(v);
	}
	/**
	 * @return long value by key
	 */
	public Long getLong(String key,Long defaultValue){
		String v=map.get(key);
		if(v==null){
			return defaultValue;
		}
		return Long.valueOf(v);
	}
	/**
	 * @return all keys
	 */
	public List<String>getKeys(){
		return new ArrayList<String>(map.keySet());
	}
	/**
	 * @return env map
	 */
	public Map<String,String>envs(){
		return map;
	}
	//--------------------------------------------------------------------------
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create().format("%-30s\n");
		map.forEach((k,v)->ib.print(k));
		return ib.toString();
	}
}
