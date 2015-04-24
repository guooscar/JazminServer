/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.rtp.RtpPacket;
import jazmin.server.sip.io.pkts.packet.rtp.impl.RtpPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RTPFramer implements Framer<TransportPacket> {

    /**
     * 
     */
    public RTPFramer() {
        // left empty intentionally
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.RTP;
    }

    /**
     * There is no real good test to make sure that the data indeed is an RTP packet. Appendix 2 in
     * RFC3550 describes one way of doing it but you really need a sequence of packets in order to
     * be able to determine if this indeed is a RTP packet or not. The best is to analyze the
     * session negotiation but here we are just looking at a single packet so can't do that.
     * 
     * Also, RTP and RTCP packets are hard to distinguish from each other and you really need help
     * from outside such as from the Session Description Protocol but until we connect things
     * together we will do a simple check so that we do not pick up RTCP packets. Note, this check
     * is not super safe, see RFC 5761.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Buffer data) throws IOException {
        // a RTP packet has at least 12 bytes. Check that
        if (data.getReadableBytes() < 12) {
            // not enough bytes but see if we actually could
            // get another 12 bytes by forcing the underlying
            // implementation to read further ahead
            data.markReaderIndex();
            try {
                final Buffer b = data.readBytes(12);
                if (b.capacity() < 12) {
                    return false;
                }
            } catch (final IndexOutOfBoundsException e) {
                // guess not...
                // System.err.println("But still!!!");
                // e.printStackTrace();
            }
            data.resetReaderIndex();
        }

        // both RTP and RTCP has a 2 in this field
        // so if that isn't the case then it cannot
        // be RTP nor RTCP
        final byte b = data.getByte(0);
        if (!((b & 0xC0) >> 6 == 0x02)) {
            return false;
        }

        // The second byte is for RTCP equal to the Packet Type (pt)
        // and if following the guidelines of RFC 5761 (section 4) then
        // RTP Payload Type + 128 != RTCP Packet Type
        final byte b2 = data.getByte(1);
        if (b2 == (byte) 0xc8 || b2 == (byte) 0xc9 || b2 == (byte) 0xca || b2 == (byte) 0xcb || b2 == (byte) 0xcc) {
            return false;
        }

        return true;
    }

    @Override
    public RtpPacket frame(final TransportPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // An RTP packet has a least 12 bytes but can contain more depending on
        // extensions, padding etc. Figure that out.
        final Buffer headers = buffer.readBytes(12);
        final Byte b = headers.getByte(0);
        final boolean hasPadding = (b & 0x20) == 0x020;
        final boolean hasExtension = (b & 0x10) == 0x010;
        final int csrcCount = b & 0x0F;

        if (hasExtension) {
            final short extensionHeaders = buffer.readShort();
            final int length = buffer.readUnsignedShort();
            final Buffer extensionData = buffer.readBytes(length);
        }

        if (hasPadding || hasExtension || csrcCount > 0) {
            // throw new RuntimeException("TODO - have not implemented the case of handling padding, extensions etc");
        }

        final Buffer payload = buffer.slice();
        return new RtpPacketImpl(parent, headers, payload);
    }
}
