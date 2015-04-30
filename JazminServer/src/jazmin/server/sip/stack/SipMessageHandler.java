/**
 * 
 */
package jazmin.server.sip.stack;

import jazmin.server.sip.SipContext;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;

/**
 * @author yama
 *
 */
public interface  SipMessageHandler {
	//
	public  boolean before(SipContext ctx)throws Exception;
	public  void handleRequest(SipContext ctx,SipRequest request)throws Exception;
	public  void handleResponse(SipContext ctx,SipResponse response)throws Exception;
	public  void after(SipContext ctx)throws Exception;
	//
	
}
