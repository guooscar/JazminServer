package jazmin.test.server.sip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipMessageAdapter;
import jazmin.server.sip.SipSession;
import jazmin.server.sip.SipStatusCode;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.address.URI;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ExpiresHeader;

/**
 * 
 * @author yama
 *
 */
public class B2BUAMessageHandler extends SipMessageAdapter {
	private static Logger logger=LoggerFactory.get(B2BUAMessageHandler.class);

	private final Map<SipURI, List<Binding>> locationStore = new HashMap<SipURI, List<Binding>>();
	//
	@Override
	public void handleRequest(SipContext ctx, SipRequest message)
			throws Exception {
		if (message.isRegister()) {
			final SipResponse response = processRegisterRequest(message);
			ctx.getConnection().send(response);
			return;
		}
		//
		if (message.isInvite()) {
			doInvite(ctx,message);
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
		ctx.proxy(response);
		//
		if(response.isRinging()){
			//change response 
		}
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
		List<Binding> toBinding=locationStore.get(toURI);
		if(toBinding==null){
			SipMessage notFoundMsg=message.createResponse(SipStatusCode.SC_NOT_FOUND);
			ctx.getConnection().send(notFoundMsg);
			return;
		}
		//
		SipSession session=ctx.getSession();
		SessionStatus ss=new SessionStatus();
		ss.originalRequest=message;
		session.setUserObject(ss);
		SipRequest forkedRequest=message;
		ctx.proxyTo(toBinding.get(0).getContact(),forkedRequest);
	}
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
	private SipResponse processRegisterRequest(final SipRequest request)
			throws NumberFormatException, IOException {
		final SipURI requestURI = (SipURI) request.getRequestUri();
		final Buffer domain = requestURI.getHost();
		final SipURI aor = getAOR(request);

		// the aor is not allowed to register under this domain
		// generate a 404 according to specfication
		// if (!validateDomain(domain, aor)) {
		// return request.createResponse(404);
		// }

		final Binding.Builder builder = Binding.with();
		builder.aor(aor);
		builder.callId(request.getCallIDHeader());
		builder.expires(getExpires(request));
		builder.cseq(request.getCSeqHeader());

		// NOTE: this is also cheating. There may be multiple contacts
		// and they must all get processed but whatever...
		builder.contact(getContactURI(request));

		final Binding binding = builder.build();
		final List<Binding> currentBindings = updateBindings(binding);
		final SipResponse response = request.createResponse(200);
		currentBindings.forEach(b -> {
			final SipURI contactURI = b.getContact();
			contactURI.setParameter("expires", b.getExpires());
			response.addHeader(ContactHeader.with(contactURI).build());
		});

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
	private List<Binding> updateBindings(final Binding binding) {
		synchronized (this.locationStore) {
			final List<Binding> bindings = ensureLocationStore(binding.getAor());
			final Iterator<Binding> it = bindings.iterator();
			boolean add = true;
			while (it.hasNext()) {
				final Binding bind = it.next();
				if (!bind.getContact().equals(binding.getContact())) {
					continue;
				}

				if (binding.getExpires() == 0) {
					it.remove();
					add = false;
				} else if (!bind.getCallId().equals(binding.getCallId())) {
					it.remove();
				} else if (binding.getCseq().getSeqNumber() > bind.getCseq()
						.getSeqNumber()) {
					it.remove();
				}
			}

			if (binding.getExpires() > 0 && add) {
				bindings.add(binding);
			}
			return bindings;
		}
	}

	private List<Binding> ensureLocationStore(final SipURI aor) {
		List<Binding> bindings = this.locationStore.get(aor);
		if (bindings == null) {
			bindings = new ArrayList<>();
			this.locationStore.put(aor, bindings);
		}
		return bindings;
	}

	private SipURI getContactURI(final SipRequest request) {
		final ContactHeader contact = request.getContactHeader();
		final URI uri = contact.getAddress().getURI();
		if (uri.isSipURI()) {
			return (SipURI) uri;
		}
		throw new IllegalArgumentException(
				"We only allow SIP URI's in the ContactHeader");
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