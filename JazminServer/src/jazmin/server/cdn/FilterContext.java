/**
 * 
 */
package jazmin.server.cdn;

import io.netty.handler.codec.http.DefaultHttpRequest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yama
 *
 */
public class FilterContext {
	public static final int CODE_OK=200;
	//
	DefaultHttpRequest request; 
	Map<String,String>responseMap;
	File requestFile;
	int errorCode;
	//
	FilterContext(File requestFile) {
		this.requestFile=requestFile;
		errorCode=CODE_OK;
		responseMap=new HashMap<String, String>();
	}
	/**
	 * @return the requestFile
	 */
	public File getRequestFile() {
		return requestFile;
	}
	//
	public String getURI(){
		return request.uri();
	}
	//
	public String getRequestHeader(String name){
		return request.headers().getAndConvert(name);
	}
	public List<String> getRequestHeaders(String name){
		return request.headers().getAllAndConvert(name);
	}
	//
	public void setResponseHeader(String key,String value){
		responseMap.put(key, value);
	}
	//
	public void setErrorCode(int errorCode){
		this.errorCode=errorCode;
	}
}
