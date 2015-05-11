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
	public boolean isRestrictRequestRate;
	public boolean isSyncOnSessionService;
	public boolean isContinuationService;

	
	@Override
	public int compareTo(IMServiceStub o) {
		return serviceId-(o.serviceId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IMServiceStub [serviceId=" + serviceId
				+ ", isRestrictRequestRate=" + isRestrictRequestRate
				+ ", isSyncOnSessionService=" + isSyncOnSessionService
				+ ", isContinuationService=" + isContinuationService + "]";
	}
	
}
