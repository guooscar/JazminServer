/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.address.URI;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.MaxForwardsHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ToHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestLine;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipRequest extends SipMessage {

    /**
     * Get the request uri of the sip request
     * 
     * @return
     */
    URI getRequestUri() throws SipParseException;

    /**
     * Pop the top-most route header.
     * 
     * This is a convenience method for calling {@link SipMessage#popHeader(Buffer)}.
     * 
     * @return the top-most {@link RouteHeader} or null if this {@link SipRequest} contained no
     *         {@link RouteHeader}s.
     */
    RouteHeader popRouteHeader();

    @Override
    SipRequest clone();

    /**
     * Factory method for creating a new INVITE request builder.
     * 
     * @param requestURI the request-uri of the INVITE request.
     * @return a {@link SipRequestBuilder}
     * @throws SipParseException in case the request uri cannot be parsed
     */
    static Builder invite(final String requestURI) throws SipParseException {
        return request(Builder.INVITE, requestURI);
    }

    static Builder ack(final String requestURI) throws SipParseException {
        return request(Builder.ACK, requestURI);
    }

    static Builder ack(final SipURI requestURI) throws SipParseException {
        assertNotNull(requestURI, "RequestURI canot be null or the empty string");
        return new Builder(Builder.ACK, requestURI);
    }

    static Builder cancel(final SipURI requestURI) throws SipParseException {
        assertNotNull(requestURI, "RequestURI canot be null or the empty string");
        return new Builder(Builder.CANCEL, requestURI);
    }

    static Builder cancel(final String requestURI) throws SipParseException {
        return request(Builder.CANCEL, requestURI);
    }

    static Builder request(final Buffer method, final String requestURI) throws SipParseException {
        assertNotEmpty(requestURI, "RequestURI canot be null or the empty string");
        try {
            final SipURI uri = SipURI.frame(Buffers.wrap(requestURI));
            return new Builder(method, uri);
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new SipParseException(0, "Unable to parse the request-uri", e);
        }
    }

    public static class Builder {

        private static final Buffer INVITE = Buffers.wrap("INVITE");
        private static final Buffer ACK = Buffers.wrap("ACK");
        private static final Buffer CANCEL = Buffers.wrap("CANCEL");

        private final Buffer method;

        private final SipURI requestURI;

        private ToHeader to;
        private FromHeader from;
        private ContactHeader contact;
        private CSeqHeader cseq;
        private MaxForwardsHeader maxForwards;
        private CallIdHeader callId;
        private ViaHeader via;
        private List<ViaHeader> vias; // after the first one, we will add Via headers to this list
        
        /**
         * 
         */
        private Builder(final Buffer method, final SipURI requestURI) {
            this.requestURI = requestURI;
            this.method = method;
        }
       
        
        public Builder to(final ToHeader to) {
            this.to = assertNotNull(to, "The To-header cannot be null");
            return this;
        }

        public Builder from(final FromHeader from) {
            this.from = assertNotNull(from, "The From-header cannot be null");
            return this;
        }

        public Builder callId(final CallIdHeader callId) {
            this.callId = assertNotNull(callId, "The Call-ID header cannot be null");
            return this;
        }

        public Builder contact(final ContactHeader contact) {
            this.contact = assertNotNull(contact, "The Contact-header cannot be null");
            return this;
        }

        public Builder cseq(final CSeqHeader cseq) {
            this.cseq = assertNotNull(cseq, "The CSeq-header cannot be null");
            return this;
        }

        /**
         * Add a via header to this request. Multiple via headers are allowed so calling this method
         * multiple times will result in all of those {@link ViaHeader}s being added to this
         * request.
         * 
         * @param via
         * @return
         */
        public Builder via(final ViaHeader via) {
            assertNotNull(via, "The Via-header cannot be null");
            if (this.via == null) {
                this.via = via;
            } else {
                ensureViaList().add(via);
            }
            return this;
        }

        /**
         * Build a new {@link SipRequest}. The only mandatory value is the request-uri and the
         * From-address. The following headers will be generated with default values unless
         * specified:
         * 
         * <ul>
         * <li><code>To</code> will be based off of the request-uri (user and host)</li>
         * <li><code>CSeq</code> will be set to 0 METHOD, e.g. 0 INVITE</li>
         * <li><code>Max-Forwards</code> will be set to 70</li>
         * <li><code>Call-ID</code> will automatically be generated</li>
         * </ul>
         * 
         * NOTE: no {@link ContactHeader} will automatically be generated since it is impossible to
         * figure out a default value that actually will work. If you are building your own SIP
         * stack you should set the {@link ContactHeader} in the transport layer before you send it
         * off.
         * 
         * @return
         * @throws SipParseException
         */
        public SipRequest build() throws SipParseException {
            assertNotNull(from, "The From-header has not been specified");
            final SipRequestLine initialLine = new SipRequestLine(method, requestURI);
            final SipRequest request = new SipRequestImpl(initialLine, null, null);
            request.setHeader(getToHeader());
            request.setHeader(from);
            request.setHeader(getCSeq());
            request.setHeader(getCallId());
            request.setHeader(getMaxForwards());
            if (via != null) {
                request.addHeader(via);
                if (this.vias != null) {
                    vias.forEach(via -> request.addHeader(via));
                }
            }
            if (contact != null) {
                request.setHeader(contact);
            }
            
            return request;
        }

        private MaxForwardsHeader getMaxForwards() {
            if (this.maxForwards == null) {
                this.maxForwards = MaxForwardsHeader.create();
            }
            return this.maxForwards;
        }

        private CallIdHeader getCallId() {
            if (this.callId == null) {
                this.callId = CallIdHeader.create();
            }
            return this.callId;
        }

        private CSeqHeader getCSeq() {
            if (this.cseq == null) {
                this.cseq = CSeqHeader.with().method(method).build();
            }
            return this.cseq;
        }

        /**
         * Get the To-header but if the user hasn't explicitly speficied one then base it off of the
         * request uri.
         * 
         * @param requestURI
         * @return
         */
        private ToHeader getToHeader() {
            if (this.to == null) {
                final Buffer user = this.requestURI.getUser();
                final Buffer host = this.requestURI.getHost();
                this.to = ToHeader.with().user(user).host(host).build();
            }
            return this.to;
        }

        private List<ViaHeader> ensureViaList() {
            if (vias == null) {
                this.vias = new ArrayList<ViaHeader>(2);
            }
            return this.vias;
        }
    }

}
