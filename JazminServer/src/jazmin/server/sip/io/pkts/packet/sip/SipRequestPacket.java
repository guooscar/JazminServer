/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip;

import jazmin.server.sip.io.pkts.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipRequestPacket extends SipPacket {

    /**
     * Get the request uri of the sip request
     * 
     * @return
     */
    URI getRequestUri() throws SipParseException;

    @Override
    SipRequestPacket clone();

}
