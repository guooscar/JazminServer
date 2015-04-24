/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.impl;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.SipHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseImpl extends SipMessageImpl implements SipResponse {

    private CSeqHeader cseq;

    /**
     * @param initialLine
     * @param headers
     * @param payload
     */
    public SipResponseImpl(final Buffer initialLine, final Buffer headers,
            final Buffer payload) {
        super(initialLine, headers, payload);
    }

    public SipResponseImpl(final SipResponseLine initialLine, final Buffer headers,
            final Buffer payload) {
        super(initialLine, headers, payload);
    }
    
    @Override
    public Buffer getReasonPhrase() {
        return getResponseLine().getReason().slice();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public Buffer getMethod() throws SipParseException {
        return getCSeqHeader().getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return getResponseLine().getStatusCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProvisional() {
        return getStatus() / 100 == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinal() {
        return getStatus() >= 200;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccess() {
        return getStatus() / 100 == 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRedirect() {
        return getStatus() / 100 == 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClientError() {
        return getStatus() / 100 == 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServerError() {
        return getStatus() / 100 == 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGlobalError() {
        return getStatus() / 100 == 6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean is100Trying() {
        return getStatus() == 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRinging() {
        return getStatus() == 180 || getStatus() == 183;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTimeout() {
        return getStatus() == 480;
    }

    @Override
    public SipResponse toResponse() throws ClassCastException {
        return this;
    }

    @Override
    public SipResponse clone() {
        throw new RuntimeException("Sorry, not implemented right now");
    }

    @Override
    public ViaHeader popViaHeader() throws SipParseException {
        final SipHeader header = popHeader(ViaHeader.NAME);
        if (header instanceof ViaHeader) {
            return (ViaHeader) header;
        }

        if (header == null) {
            return null;
        }


        final Buffer buffer = header.getValue();
        return ViaHeader.frame(buffer);
    }

}
