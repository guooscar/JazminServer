/**
 * 
 */
package jazmin.server.msg;

import java.lang.reflect.Method;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class ServiceStub implements Comparable<ServiceStub>{
	public String serviceId;
	public Object instance;
	public Method method;
	public boolean isSyncOnSessionService;
	public boolean isDisableResponseService;
	public boolean isContinuationService;
	public boolean isRestrictRequestRate;

	
	@Override
	public int compareTo(ServiceStub o) {
		return serviceId.compareTo(o.serviceId);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceStub [serviceId=" + serviceId
				+ ", isSyncOnSessionService=" + isSyncOnSessionService
				+ ", isDisableResponseService=" + isDisableResponseService
				+ ", isContinuationService=" + isContinuationService
				+ ", isRestrictRequestRate=" + isRestrictRequestRate + "]";
	}
	
}
