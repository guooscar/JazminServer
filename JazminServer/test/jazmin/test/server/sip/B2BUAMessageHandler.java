package jazmin.test.server.sip;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.search.ReceivedDateTerm;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipMessageAdapter;
import jazmin.server.sip.SipSession;
import jazmin.server.sip.SipStatusCode;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.address.URI;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ExpiresHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;

/**
 * 
 * @author yama
 *
 */
public class B2BUAMessageHandler extends SipMessageAdapter {
	private static Logger logger=LoggerFactory.get(B2BUAMessageHandler.class);

	private final Map<SipURI, Binding> locationStore = new HashMap<SipURI, Binding>();
	//
	@Override
	public void handleRequest(SipContext ctx, SipRequest message)
			throws Exception {
		if (message.isRegister()) {
			doRegister(ctx, message);
			dumpStore();
			return;
		}
		//
		if (message.isInvite()) {
			doInvite(ctx,message);
			return;
		}
		if(message.isAck()){
			if(ctx.getSession(false)!=null){
				ctx.getSession(false).invalidate();
			}
			return;
		}
		//
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_OK));
	}
	//
	@Override
	public void handleResponse(SipContext ctx, SipResponse response)
			throws Exception {
		SessionStatus ss=(SessionStatus) ctx.getSession().getUserObject();
		if(ss==null){
			return;
		}
		response.popViaHeader();
		ctx.getServer().send(ss.remoteAddress, ss.remotePort, response);
	}
	//
	private void dumpStore(){
		StringBuilder sb=new StringBuilder();
		locationStore.forEach((uri,blist)->{
			sb.append(uri+"\t->\t"+blist);
			sb.append("\n");
		});
		logger.debug("\n"+sb);
	}
	//
	private void doInvite(SipContext ctx,SipRequest message)throws Exception{
		dumpStore();
		Address toAddress=message.getToHeader().getAddress();
		URI toURI=toAddress.getURI();
		Binding toBinding=locationStore.get(toURI);
		if(toBinding==null){
			SipMessage notFoundMsg=message.createResponse(SipStatusCode.SC_NOT_FOUND);
			ctx.getConnection().send(notFoundMsg);
			return;
		}
		//
		SipSession session=ctx.getSession();
		SessionStatus ss=new SessionStatus();
		ss.originalRequest=message;
		ss.remoteAddress=ctx.getConnection().getRemoteIpAddress();
		ss.remotePort=ctx.getConnection().getRemotePort();
		session.setUserObject(ss);
		SipRequest forkedRequest=message;
		ctx.getServer().proxyTo(toBinding.getContact(),forkedRequest);
	}
	//
	private void doRegister(SipContext ctx, SipRequest message)throws Exception{
		final SipResponse response = processRegisterRequest(ctx,message);
		ctx.getConnection().send(response);
	}
	//
	//
	/**
	 * Section 10.3 in RFC3261 outlines how to process a register request. For
	 * the purpose of this little exercise, we are skipping many steps just to
	 * keep things simple.
	 * 
	 * @param request
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private SipResponse processRegisterRequest(SipContext ctx,final SipRequest request)
			throws NumberFormatException, IOException {
		final SipURI aor = getAOR(request);
		final Binding.Builder builder = Binding.with();
		builder.aor(aor);
		builder.callId(request.getCallIDHeader());
		builder.expires(getExpires(request));
		builder.cseq(request.getCSeqHeader());

		// NOTE: this is also cheating. There may be multiple contacts
		// and they must all get processed but whatever...
		SipURI newURI=SipURI.with().
				host(ctx.getConnection().getRemoteIpAddress()).
				port(ctx.getConnection().getRemotePort()).
				build();
		builder.contact(newURI);
		final Binding binding = builder.build();
		final Binding b = updateBindings(binding);
		final SipResponse response = request.createResponse(200);
		final SipURI contactURI = b.getContact();
		contactURI.setParameter("expires", b.getExpires());
		ViaHeader vh=response.getViaHeader();
		vh.setRPort(ctx.getConnection().getRemotePort());
		vh.setReceived(Buffers.wrap(ctx.getConnection().getRemoteIpAddress()));
		response.addHeader(ContactHeader.with(contactURI).build());
		return response;
	}

	/**
	 * See RFC3261 of how it is actually supposed to be done but the short
	 * version is:
	 * 
	 * For the AOR, compare the contact URI of all known bidnings and
	 * update/create/delete as needed.
	 * 
	 * 
	 * @param binding
	 * @return
	 */
	private Binding updateBindings(final Binding binding) {
		if(binding.getExpires()==0){
			locationStore.remove(binding);
		}else{
			locationStore.put(binding.getAor(),binding);
		}
		return binding;
	}
	//
	private int getExpires(final SipRequest request)
			throws NumberFormatException, IOException {
		final ContactHeader contact = request.getContactHeader();
		if (contact != null) {
			final Buffer value = contact.getParameter("expires");
			if (value != null) {
				return value.parseToInt();
			}
		}
		final ExpiresHeader expires = request.getExpiresHeader();
		return expires.getExpires();
	}
	/**
	 * The To-header contains the AOR (address-of-record) that the user wish to
	 * associate with the contact information in the Contact-header. We must
	 * also convert the To-header into its canonical form, which is the aor we
	 * will use as the key into the existing bindings.
	 * 
	 * @param request
	 * @return
	 */
	private SipURI getAOR(final SipRequest request) {
		final SipURI sipURI = (SipURI) request.getToHeader().getAddress()
				.getURI();
		return SipURI.with().user(sipURI.getUser()).host(sipURI.getHost())
				.build();
	}

}