/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.impl;

import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.SipResponsePacket;

/**
 * @author jonas
 * 
 */
public class SipResponsePacketImpl extends SipPacketImpl implements SipResponsePacket {

    private final SipResponse response;

    /**
     * @param transportPacket
     * @param msg
     */
    public SipResponsePacketImpl(final TransportPacket transportPacket, final SipResponse msg) {
        super(transportPacket, msg);
        this.response = msg;
    }

    @Override
    public SipResponsePacket toResponse() throws ClassCastException {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#getStatus()
     */
    @Override
    public int getStatus() {
        return this.response.getStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isProvisional()
     */
    @Override
    public boolean isProvisional() {
        return this.response.isProvisional();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isSuccess()
     */
    @Override
    public boolean isSuccess() {
        return this.response.isSuccess();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isRedirect()
     */
    @Override
    public boolean isRedirect() {
        return this.response.isRedirect();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isClientError()
     */
    @Override
    public boolean isClientError() {
        return this.response.isClientError();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isServerError()
     */
    @Override
    public boolean isServerError() {
        return this.response.isServerError();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isGlobalError()
     */
    @Override
    public boolean isGlobalError() {
        return this.response.isGlobalError();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#is100Trying()
     */
    @Override
    public boolean is100Trying() {
        return this.response.is100Trying();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isRinging()
     */
    @Override
    public boolean isRinging() {
        return this.response.isRinging();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipResponsePacket#isTimeout()
     */
    @Override
    public boolean isTimeout() {
        return this.response.isTimeout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.impl.SipPacketImpl#clone()
     */
    @Override
    public SipResponsePacket clone() {
        final TransportPacket transport = getTransportPacket().clone();
        final SipResponse response = this.response.clone();
        return new SipResponsePacketImpl(transport, response);
    }

}
