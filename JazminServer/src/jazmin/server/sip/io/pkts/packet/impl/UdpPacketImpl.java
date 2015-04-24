/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.UDPPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class UdpPacketImpl extends TransportPacketImpl implements UDPPacket {

    private final Buffer headers;

    private final IPPacket parent;

    /**
     * @param parent
     * @param headers
     */
    public UdpPacketImpl(final IPPacket parent, final Buffer headers, final Buffer payload) {
        super(parent, Protocol.UDP, headers, payload);
        this.parent = parent;
        this.headers = headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUDP() {
        return true;
    }

    public int getLength() {
        return this.headers.getUnsignedShort(4);
    }

    public void setLength(final int length) {
        this.headers.setUnsignedShort(4, length);
    }

    public int getChecksum() {
        return this.headers.getUnsignedShort(6);
    }

    @Override
    public TransportPacket clone() {
        final IPPacket parent = getParent().clone();
        return new UdpPacketImpl(parent, this.headers.clone(), getPayload().clone());
    }

    @Override
    public final void write(final OutputStream out, final Buffer payload) throws IOException {
        // Note: because the Buffers.wrap will copy the bytes we cannot change the length
        // in this.headers after we have wrapped them. Simply wont work...
        final int size = this.headers.getReadableBytes() + (payload != null ? payload.getReadableBytes() : 0);
        this.setLength(size);
        reCalculateChecksum();
        final Buffer pkt = Buffers.wrap(this.headers, payload);
        this.parent.write(out, pkt);
    }

}
