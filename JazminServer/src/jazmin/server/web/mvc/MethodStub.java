/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.Method;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class MethodStub implements Comparable<MethodStub>{
	public String controllerId;
	public String id;
	int queryCount;
	public String method;
	public Method invokeMethod;
	public boolean syncOnSession;
	//
	@Override
	public int compareTo(MethodStub o) {
		return (controllerId+"."+id).compareTo(o.controllerId+"."+id);
	}
	//
	@Override
	public String toString() {
		return "["+controllerId+"/"+id+":"+method+"]-"+
				invokeMethod.getDeclaringClass().getSimpleName()+
				"."+invokeMethod.getName()+" syncOnSession:"+syncOnSession;
	}
}
