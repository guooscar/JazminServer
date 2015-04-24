/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.UDPPacket;
import jazmin.server.sip.io.pkts.packet.impl.UdpPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class UDPFramer implements Framer<IPPacket> {

    /**
     * 
     */
    public UDPFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.UDP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UDPPacket frame(final IPPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // UDP header is very simple. For our purposes, just
        // read the 8 bytes containing all the header fields
        // and the rest is just user data (payload of the udp packet)
        final Buffer headers = buffer.readBytes(8);
        final Buffer payload = buffer.slice();
        if (payload.isEmpty()) {
            return new UdpPacketImpl(parent, headers, null);
        }
        return new UdpPacketImpl(parent, headers, payload);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
