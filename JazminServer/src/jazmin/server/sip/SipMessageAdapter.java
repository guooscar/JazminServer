package jazmin.server.sip;

import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;
import jazmin.server.sip.stack.SipMessageHandler;

/**
 * 
 * @author yama
 *
 */
public class SipMessageAdapter implements SipMessageHandler{

	@Override
	public boolean before(SipContext ctx) throws Exception {
		return true;
	}

	@Override
	public void handleRequest(SipContext ctx, SipRequest request)
			throws Exception {
		
	}

	@Override
	public void handleResponse(SipContext ctx, SipResponse response)
			throws Exception {
		
	}

	@Override
	public void after(SipContext ctx) throws Exception {
		
	}

}
