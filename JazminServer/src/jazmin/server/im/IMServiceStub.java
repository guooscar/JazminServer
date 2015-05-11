/**
 * 
 */
package jazmin.server.im;

import java.lang.reflect.Method;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class IMServiceStub implements Comparable<IMServiceStub>{
	public int serviceId;
	public Object instance;
	public Method method;
	public boolean isSyncOnSessionService;
	public boolean isContinuationService;
	
	@Override
	public int compareTo(IMServiceStub o) {
		return serviceId-(o.serviceId);
	}
	//
	@Override
	public String toString() {
		return "[isSyncOnSession=" + isSyncOnSessionService
				+ ", isContinuation=" + isContinuationService + "]";
	}
}
