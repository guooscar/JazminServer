/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.MACPacket;
import jazmin.server.sip.io.pkts.packet.impl.IPPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPv4Framer implements Framer<MACPacket> {

    public IPv4Framer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.IPv4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPPacket frame(final MACPacket parent, final Buffer payload) throws IOException {

        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // the ipv4 headers are always 20 bytes unless
        // the length is greater than 5
        final Buffer headers = payload.readBytes(20);

        // byte 1, contains the version and the length
        final byte b = headers.getByte(0);
        // final int version = ((i >>> 28) & 0x0F);
        // final int length = ((i >>> 24) & 0x0F);

        final int version = b >>> 5 & 0x0F;
        final int length = b & 0x0F;

        // byte 2 - dscp and ecn
        // final byte b2 = headers.readByte();

        // final int dscp = ((b2 >>> 6) & 0x3B);
        // final int ecn = (b2 & 0x03);

        // byte 3 - 4
        // final int totalLength = headers.readUnsignedShort();

        // byte 5 - 6
        // final short id = headers.readShort();

        // this one contains flags + fragment offset

        // byte 7 - 8
        // final short flagsAndFragement = headers.readShort();

        // byte 9
        // final byte ttl = headers.readByte();

        // byte 10
        // final byte protocol = headers.getByte(9);

        // byte 11 - 12
        // final int checkSum = headers.readUnsignedShort();

        // byte 13 - 16
        // final int sourceIp = headers.readInt();

        // byte 17 - 20
        // final int destIp = headers.readInt();

        // if the length is greater than 5, then the frame
        // contains extra options so read those as well
        int options = 0;
        if (length > 5) {
            // remember, this may have to be treated as unsigned
            // final int options = headers.readInt();
            options = payload.readInt();
        }

        final Buffer data = payload.slice();
        return new IPPacketImpl(parent, headers, options, data);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
