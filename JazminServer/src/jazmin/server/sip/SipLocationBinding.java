package jazmin.server.sip;
import java.util.Date;

import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
/**
 * 
 * @author yama
 *
 */
public class SipLocationBinding {
    private final SipURI aor;
    private final int expires;
    private final CSeqHeader cseq;
    private final SipURI contact;
    private final CallIdHeader callId;
    private final Date createTime;
    //
    private SipLocationBinding(final SipURI aor, final int expires, final CSeqHeader cseq, final SipURI contact,
            final CallIdHeader callId) {
        this.aor = aor;
        this.expires = expires;
        this.cseq = cseq;
        this.contact = contact.clone();
        this.callId = callId;
        this.createTime=new Date();
    }
    //
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

    /**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}
	@Override
    public String toString() {
        return this.contact.toString();
    }

    public static Builder with() {
        return new Builder();
    }
    //--------------------------------------------------------------------------
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

        public SipLocationBinding build() {
            // of course, we really should validate things here
            // but since this is a basic example, we will ignore
            // this for now
            return new SipLocationBinding(this.aor, this.expires, this.cseq, this.contact, this.callId);
        }

    }

}