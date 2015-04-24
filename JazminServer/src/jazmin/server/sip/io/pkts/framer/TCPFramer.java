/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.TCPPacket;
import jazmin.server.sip.io.pkts.packet.impl.TcpPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class TCPFramer implements Framer<IPPacket> {

    /**
     * 
     */
    public TCPFramer() {
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
    public TCPPacket frame(final IPPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // The TCP header is at least 20 bytes and if there are options
        // there can be an additional 40 bytes. The offset will tell us
        // how may 32-bit words there are.

        final Buffer headers = buffer.readBytes(20);
        Buffer options = null;
        Buffer payload = null;

        final byte offset = headers.getByte(12);

        // once again, the minimum size of a tcp header is 5 words
        // and since we already have read that off the buffer, this
        // is what we have left to read
        final int size = (offset >> 4 & 0x0F) - 5;
        if (size > 0) {
            options = buffer.readBytes(size * 4);
        }

        // to handle packets that has no payload (e.g a syn packet)
        if (buffer.hasReadableBytes()) {
            payload = buffer.slice();
        }
        return new TcpPacketImpl(parent, headers, options, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Buffer data) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

}
