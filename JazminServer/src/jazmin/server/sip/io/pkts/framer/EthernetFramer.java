/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.frame.UnknownEtherType;
import jazmin.server.sip.io.pkts.packet.MACPacket;
import jazmin.server.sip.io.pkts.packet.PCapPacket;
import jazmin.server.sip.io.pkts.packet.impl.MACPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * Simple framer for framing Ethernet frames
 * 
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramer implements Framer<PCapPacket> {

    public EthernetFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.ETHERNET_II;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MACPacket frame(final PCapPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // final Buffer destMacAddress = buffer.readBytes(6);
        // final Buffer srcMacAddress = buffer.readBytes(6);
        // final byte b1 = buffer.readByte();
        // final byte b2 = buffer.readByte();

        final Buffer headers = buffer.readBytes(14);
        final byte b1 = headers.getByte(12);
        final byte b2 = headers.getByte(13);

        try {
            getEtherType(b1, b2);
        } catch (final UnknownEtherType e) {
            throw new RuntimeException("uknown ether type");
        }

        final Buffer payload = buffer.slice(buffer.capacity());
        return new MACPacketImpl(Protocol.ETHERNET_II, parent, headers, payload);
    }

    public static EtherType getEtherType(final byte b1, final byte b2) throws UnknownEtherType {
        final EtherType type = getEtherTypeSafe(b1, b2);
        if (type != null) {
            return type;
        }

        // will implement as we need to
        throw new UnknownEtherType(b1, b2);
    }

    public static EtherType getEtherTypeSafe(final byte b1, final byte b2) {
        if (b1 == (byte) 0x08 && b2 == (byte) 0x00) {
            return EtherType.IPv4;
        } else if (b1 == (byte) 0x86 && b2 == (byte) 0xdd) {
            return EtherType.IPv6;
        }

        return null;
    }

    @Override
    public boolean accept(final Buffer data) {
        return false;
    }

    public static enum EtherType {
        IPv4((byte) 0x08, (byte) 0x00), IPv6((byte) 0x86, (byte) 0xdd);

        private final byte b1;
        private final byte b2;

        private EtherType(final byte b1, final byte b2) {
            this.b1 = b1;
            this.b2 = b2;
        }

        public void write(final OutputStream out) throws IOException {
            out.write(this.b1);
            out.write(this.b2);
        }
    }

}
