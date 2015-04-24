/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

import java.io.IOException;

import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SipFilter implements Filter {

    /**
     * 
     */
    public SipFilter() {
        // left empty intentionally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Packet packet) throws FilterException {
        try {
            return packet.hasProtocol(Protocol.SIP);
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        }
    }
}
