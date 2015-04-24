package jazmin.test.server.sip;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
/**
 * Represents an association between the AOR and a contact address where this AOR can be reached.
 * 
 * Note, this is a simplified version and doesn't contain e.g. Path headers, which are crucial for a
 * real network.
 * 
 * @author jonas@jonasborjesson.com
 */
public class Binding {

    private final SipURI aor;

    private final int expires;

    private final CSeqHeader cseq;

    private final SipURI contact;

    private final CallIdHeader callId;

    private Binding(final SipURI aor, final int expires, final CSeqHeader cseq, final SipURI contact,
            final CallIdHeader callId) {
        this.aor = aor;
        this.expires = expires;
        this.cseq = cseq;
        this.contact = contact.clone();
        this.callId = callId;
    }

    public SipURI getAor() {
        return this.aor;
    }

    public int getExpires() {
        return this.expires;
    }

    public CSeqHeader getCseq() {
        return this.cseq;
    }

    public SipURI getContact() {
        return this.contact.clone();
    }

    public CallIdHeader getCallId() {
        return this.callId;
    }

    @Override
    public String toString() {
        return this.contact.toString();
    }

    public static Builder with() {
        return new Builder();
    }

    public static class Builder {

        private SipURI aor;

        private int expires;

        private CSeqHeader cseq;

        private SipURI contact;

        private CallIdHeader callId;

        private Builder() {
            // just to prevent instantiation
        }

        public Builder callId(final CallIdHeader callId) {
            this.callId = callId;
            return this;
        }

        public Builder aor(final SipURI aor) {
            this.aor = aor;
            return this;
        }

        public Builder expires(final int expires) {
            this.expires = expires;
            return this;
        }

        public Builder cseq(final CSeqHeader cseq) {
            this.cseq = cseq;
            return this;
        }

        public Builder contact(final SipURI contact) {
            this.contact = contact;
            return this;
        }

        public Binding build() {
            // of course, we really should validate things here
            // but since this is a basic example, we will ignore
            // this for now
            return new Binding(this.aor, this.expires, this.cseq, this.contact, this.callId);
        }

    }

}