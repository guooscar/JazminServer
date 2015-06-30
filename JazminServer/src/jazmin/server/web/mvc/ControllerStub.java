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
	Method beforeMethod;
	Method afterMethod;
	
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
	void addServiceMethod(Service srv,Method m){
		if(!Modifier.isPublic(m.getModifiers())){
			throw new IllegalArgumentException("service method:"
					+m.getName()+" must be public");
		}
		//
		if(m.getParameterTypes().length!=1||
				!m.getParameterTypes()[0].equals(Context.class)){
			throw new IllegalArgumentException("parameter must be Context.");		
		}
		if(srv.id()==null){
			throw new IllegalArgumentException("service id can not be null.");	
		}
		MethodStub ms=new MethodStub();
		ms.controllerId=id;
		ms.id=srv.id();
		ms.queryCount=srv.queryCount();
		ms.method=srv.method().toString();
		ms.invokeMethod=m;
		ms.syncOnSession=srv.syncOnSession();
		if(methodStubs.containsKey(srv.id())){
			throw new IllegalArgumentException("service :"+srv.id()+" already exists");
		}
		methodStubs.put(srv.id(),ms);
		//
		if(srv.index()){
			indexMethod=ms;
		}
	}
	//
	void addBeforeServiceMethod(BeforeService srv,Method m){
		if(!Modifier.isPublic(m.getModifiers())){
			throw new IllegalArgumentException("brfore service method:"
					+m.getName()+" must be public");
		}
		//
		if(m.getParameterTypes().length!=1||
				!m.getParameterTypes()[0].equals(Context.class)){
			throw new IllegalArgumentException("parameter must be Context.");		
		}
		if(!m.getReturnType().equals(Boolean.class)&&
				!m.getReturnType().equals(boolean.class)){
			throw new IllegalArgumentException("return type must be boolean");		
		}
		beforeMethod=m;
	}
	//
	void addAfterServiceMethod(AfterService srv,Method m){
		if(!Modifier.isPublic(m.getModifiers())){
			throw new IllegalArgumentException("after service method:"
					+m.getName()+" must be public");
		}
		//
		if(m.getParameterTypes().length!=2||
				!(m.getParameterTypes()[0].equals(Context.class)&&
						m.getParameterTypes()[1].equals(Throwable.class))){
			throw new IllegalArgumentException("parameter must be (Context,Throwable)");		
		}
		afterMethod=m;
	}
	//
	void setInstance(Object instance){
		this.instance=instance;
		//
		for(Method m:instance.getClass().getMethods()){
			Service srv=m.getDeclaredAnnotation(Service.class);
			if(srv!=null){
				addServiceMethod(srv, m);
			}
			BeforeService beforeSrv=m.getDeclaredAnnotation(BeforeService.class);
			if(beforeSrv!=null){
				addBeforeServiceMethod(beforeSrv, m);
			}
			AfterService afterService=m.getDeclaredAnnotation(AfterService.class);
			if(afterService!=null){
				addAfterServiceMethod(afterService, m);
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
		String before=beforeMethod==null?"null":beforeMethod.getName();
		String after=afterMethod==null?"null":afterMethod.getName();
		return "["+id+"] before:"+before+"/"+"after:"+after;
	}
}