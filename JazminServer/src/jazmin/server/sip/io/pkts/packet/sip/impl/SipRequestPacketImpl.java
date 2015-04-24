/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.impl;

import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipRequestPacket;
import jazmin.server.sip.io.pkts.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestPacketImpl extends SipPacketImpl implements SipRequestPacket {

    /**
     * @param transportPacket
     * @param msg
     */
    public SipRequestPacketImpl(final TransportPacket transportPacket, final SipRequest request) {
        super(transportPacket, request);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipRequestPacket#getRequestUri()
     */
    @Override
    public URI getRequestUri() throws SipParseException {
        return ((SipRequest) getSipMessage()).getRequestUri();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toRequest()
     */
    @Override
    public SipRequestPacket toRequest() throws ClassCastException {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.impl.SipPacketImpl#clone()
     */
    @Override
    public SipRequestPacket clone() {
        final TransportPacket transport = getTransportPacket().clone();
        final SipRequest request = (SipRequest) getSipMessage().clone();
        return new SipRequestPacketImpl(transport, request);
    }

}
