/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public class UnknownApplicationPacketImpl extends AbstractApplicationPacket {

    /**
     * @param p
     * @param parent
     * @param payload
     */
    public UnknownApplicationPacketImpl(final TransportPacket parent, final Buffer payload) {
        super(Protocol.UNKNOWN, parent, payload);
    }

    @Override
    public Packet getNextPacket() throws IOException {
        // We can't even figure out what this packet is so there is no
        // way we would be able to figure out if this unkown packet
        // has a body of some sort and what potentially that payload would
        // be... hence, returning null
        return null;
    }

    @Override
    public UnknownApplicationPacketImpl clone() {
        return new UnknownApplicationPacketImpl(getParent(), getPayload());
    }

    @Override
    public boolean isUDP() {
        return getParent().isUDP();
    }

    @Override
    public boolean isTCP() {
        return getParent().isTCP();
    }

}
