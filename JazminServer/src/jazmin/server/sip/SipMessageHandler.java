/**
 * 
 */
package jazmin.server.sip;

import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;

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
