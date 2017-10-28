/**
 * 
 */
package jazmin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yama
 *
 */
public class MapMaker {
	Map<String, Object>map;
	MapMaker(){
		map=new HashMap<String, Object>();;
	}
	public static MapMaker map(){
		MapMaker mm=new MapMaker();
		return mm;
	}
	//
	public MapMaker put(String key,Object value){
		map.put(key, value);
		return this;
	}
	//
	public Map<String,Object>get(){
		return map;
	}
}
