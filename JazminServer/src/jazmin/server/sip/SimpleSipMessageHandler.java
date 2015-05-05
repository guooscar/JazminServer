/**
 * 
 */
package jazmin.server.sip;

import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;

/**
 * @author yama
 *
 */
public class SimpleSipMessageHandler extends SipMessageAdapter {

	@Override
	public boolean before(SipContext ctx) throws Exception {
		return true;
	}

	@Override
	public void handleRequest(SipContext ctx, SipRequest request)
			throws Exception {
		if(request.isRegister()){
			doRegister(ctx, request);
		}else if(request.isInvite()){
			doInvite(ctx, request);
		}else if(request.isOptions()){
			doOptions(ctx, request);
		}else if(request.isUpdate()){
			doUpdate(ctx, request);
		}else if(request.isBye()){
			doBye(ctx, request);
		}else if(request.isInfo()){
			doInfo(ctx, request);
		}else if(request.isMessage()){
			doMessage(ctx, request);
		}else if(request.isCancel()){
			doCancel(ctx, request);
		}else if(request.isAck()){
			doAck(ctx, request);
		}else if(request.isPublish()){
			doPublish(ctx, request);
		}else{
			throw new IllegalArgumentException("unsupport message type:"
					+request.getMethod().toString());
		}
	}

	@Override
	public void handleResponse(SipContext ctx, SipResponse response)
			throws Exception {

	}

	@Override
	public void after(SipContext ctx) throws Exception {

	}
	//
	public void doUpdate(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}
	//
	public void doRegister(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doInvite(SipContext ctx, SipRequest request) throws Exception{
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doMessage(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doAck(SipContext ctx, SipRequest request) throws Exception{
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doBye(SipContext ctx, SipRequest request) throws Exception{
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doOptions(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doInfo(SipContext ctx, SipRequest request) throws Exception{
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}

	public void doCancel(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}
	
	public void doPublish(SipContext ctx, SipRequest request)throws Exception {
		ctx.getConnection().send(request.createResponse(SipStatusCode.SC_OK));
	}
}
