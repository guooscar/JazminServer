/**
 * 
 */
package jazmin.server.web.mvc;

import java.util.HashMap;
import java.util.Map;

import jazmin.util.DumpIgnore;

/**
 * @author yama
 * 29 Dec, 2014
 */
@DumpIgnore
public class Context {
	View view;
	Throwable exception;
	Request request;
	Response response;
	Map<String,Object>contextMap;
	//
	public Context() {
		contextMap=new HashMap<String, Object>();
	}
	//
	public void put(String key,Object v){
		contextMap.put(key, v);
	}
	//
	public View view(){
		return view;
	}
	//
	public View view(View v){
		this.view=v;
		return v;
	}
	//
	public Request request(){
		return request;
	}
	//
	public Response response(){
		return response;
	}
	//
	public Boolean  getBoolean(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Boolean.valueOf(ss);
	}
	public String  getString(String key){	
		String ss=request.queryParams(key);
		return ss;
	}
	public Long  getLong(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Long.valueOf(ss);
	}
	public Integer  getInteger(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Integer.valueOf(ss);
	}
	public Short  getShort(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Short.valueOf(ss);
	}
	public Float  getFloat(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Float.valueOf(ss);
	}
	public Double  getDouble(String key){
		String ss=request.queryParams(key);
		return ss==null?null:Double.valueOf(ss);
	}
	//
	@Override
	public String toString() {
		return request.toString();
	}
}
