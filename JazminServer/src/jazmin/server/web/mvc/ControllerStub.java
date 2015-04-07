/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class ControllerStub implements Comparable<ControllerStub>{
	Map<String,MethodStub>methodStubs;
	MethodStub indexMethod;
	Object instance;
	String id;
	public ControllerStub(String id) {
		this.id=id;
		methodStubs=new ConcurrentHashMap<String, MethodStub>();
	}
	//
	MethodStub getMethod(String id){
		return methodStubs.get(id);
	}
	//
	void setInstance(Object instance){
		this.instance=instance;
		//
		for(Method m:instance.getClass().getDeclaredMethods()){
			Service srv=m.getDeclaredAnnotation(Service.class);
			if(srv==null){
				continue;
			}
			if(!Modifier.isPublic(m.getModifiers())){
				throw new IllegalArgumentException("service method:"
						+m.getName()+" must be public");
			}
			//
			if(m.getParameterTypes().length<1||
					!m.getParameterTypes()[0].equals(Context.class)){
				throw new IllegalArgumentException("parameter must be Context.");		
			}
			if(srv.id()==null){
				throw new IllegalArgumentException("service id can not be null.");	
			}
			MethodStub ms=new MethodStub();
			ms.controllerId=id;
			ms.id=srv.id();
			ms.method=srv.method().toString();
			ms.invokeMethod=m;
			if(methodStubs.containsKey(srv.id())){
				throw new IllegalArgumentException("service :"+srv.id()+" already exists");
			}
			methodStubs.put(srv.id(),ms);
			//
			if(srv.index()){
				indexMethod=ms;
			}
		}
	}
	//
	public List<MethodStub>methodStubs(){
		return new ArrayList<MethodStub>(methodStubs.values());
	}
	//
	public int compareTo(ControllerStub o) {
		return id.compareTo(o.id);
	};
	//
	@Override
	public String toString() {
		return id;
	}
}