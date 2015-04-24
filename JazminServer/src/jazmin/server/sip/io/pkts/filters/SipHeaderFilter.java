/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.PacketParseException;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.packet.sip.header.SipHeader;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderFilter extends SipFilter {
    private final Buffer name;
    private final Buffer value;

    public SipHeaderFilter(final String name, final String value) {
        this.name = Buffers.wrap(name);
        this.value = Buffers.wrap(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Packet packet) throws FilterException {
        try {
            if (super.accept(packet)) {
                final SipPacket msg = (SipPacket) packet.getPacket(Protocol.SIP);
                final SipHeader header = msg.getHeader(this.name);
                if (header == null) {
                    return false;
                }

                return header.getValue().equals(this.value);
            }
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        }
        return false;
    }

}
