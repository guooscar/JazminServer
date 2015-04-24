package jazmin.server.sip.io.pkts.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public final class PcapGlobalHeader {

    /**
     * See http://wiki.wireshark.org/Development/LibpcapFileFormat
     */
    public static final byte[] MAGIC_BIG_ENDIAN = { (byte) 0xa1, (byte) 0xb2, (byte) 0xc3, (byte) 0xd4 };

    /**
     * See http://wiki.wireshark.org/Development/LibpcapFileFormat
     */
    public static final byte[] MAGIC_LITTLE_ENDIAN = { (byte) 0xd4, (byte) 0xc3, (byte) 0xb2, (byte) 0xa1 };

    /**
     * New pcap format
     */
    public static final byte[] MAGIC_NGPCAP = { (byte) 0x0A, (byte) 0x0D, (byte) 0x0D, (byte) 0x0A };

    /**
     * Found the following at:
     * http://anonsvn.wireshark.org/wireshark/trunk/wiretap/libpcap.h
     * 
     * PCAP_NSEC_MAGIC is for Ulf Lamping's modified "libpcap" format, which
     * uses the same common file format as PCAP_MAGIC, but the timestamps are
     * saved in nanosecond resolution instead of microseconds.
     */
    public static final byte[] MAGIC_NSEC = { (byte) 0xa1, (byte) 0xb2, (byte) 0x3c, (byte) 0x4d };
    public static final byte[] MAGIC_NSEC_SWAPPED = { (byte) 0x4d, (byte) 0x3c, (byte) 0xb2, (byte) 0xa2 };

    /**
     * Found the following at:
     * http://anonsvn.wireshark.org/wireshark/trunk/wiretap/libpcap.h
     * 
     * 
     * PCAP_MODIFIED_MAGIC is for Alexey Kuznetsov's modified "libpcap" format,
     * as generated on Linux systems that have a "libpcap" with his patches, at
     * http://ftp.sunet.se/pub/os/Linux/ip-routing/lbl-tools/
     * 
     * applied; PCAP_SWAPPED_MODIFIED_MAGIC is the byte-swapped version.
     */
    public static final byte[] MAGIC_MODIFIED = { (byte) 0xa1, (byte) 0xb2, (byte) 0xcd, (byte) 0x34 };
    public static final byte[] MAGIC_MODIFIED_SWAPPED = { (byte) 0x34, (byte) 0xcd, (byte) 0xb2, (byte) 0xa1 };

    private final ByteOrder byteOrder;
    private final byte[] body;

    /**
     * Factory method for creating a default {@link PcapGlobalHeader}. Mainly
     * used for when writing out new pcaps to a stream.
     * 
     * @return
     */
    public static PcapGlobalHeader createDefaultHeader() {
        return createDefaultHeader(Protocol.ETHERNET_II);
    }

    public static PcapGlobalHeader createDefaultHeader(final Protocol protocol) {
        final byte[] body = new byte[20];

        // major version number
        body[1] = (byte) 0x00;
        body[0] = (byte) 0x02;

        // minor version number
        body[3] = (byte) 0x00;
        body[2] = (byte) 0x04;

        // GMT to local correction - in practice always zero
        body[4] = (byte) 0x00;
        body[5] = (byte) 0x00;
        body[6] = (byte) 0x00;
        body[7] = (byte) 0x00;

        // accuracy of timestamp - always zero.
        body[8] = (byte) 0x00;
        body[9] = (byte) 0x00;
        body[10] = (byte) 0x00;
        body[11] = (byte) 0x00;

        // snaplength - typically 65535
        body[12] = (byte) 0xFF;
        body[13] = (byte) 0xFF;
        body[14] = (byte) 0x00;
        body[15] = (byte) 0x00;

        // data link type - default is ethernet
        // See http://www.tcpdump.org/linktypes.html for a complete list
        if (protocol == null || protocol == Protocol.ETHERNET_II) {
            body[16] = (byte) 0x01;
            body[17] = (byte) 0x00;
            body[18] = (byte) 0x00;
            body[19] = (byte) 0x00;
        } else if (protocol == null || protocol == Protocol.SLL) {
            body[16] = (byte) 0x71;
            body[17] = (byte) 0x00;
            body[18] = (byte) 0x00;
            body[19] = (byte) 0x00;
        } else {
            throw new IllegalArgumentException("Unknown protocol \"" + protocol
                    + "\". Not sure how to construct the global header. You probably need to add some code yourself");
        }

        return new PcapGlobalHeader(ByteOrder.LITTLE_ENDIAN, body);

    }

    public PcapGlobalHeader(final ByteOrder byteOrder, final byte[] body) {
        assert byteOrder != null;
        assert body != null && body.length == 20;
        this.byteOrder = byteOrder;
        this.body = body;
    }

    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    /**
     * Major version is currently 2
     * 
     * @return
     */
    public int getMajorVersion() {
        return getUnsignedShort(0, this.body, this.byteOrder);
    }

    /**
     * Minor version is currently 4
     * 
     * @return
     */
    public int getMinorVersion() {
        return getUnsignedShort(2, this.body, this.byteOrder);
    }

    /**
     * in theory, the accuracy of time stamps in the capture; in practice, all
     * tools set it to 0
     * 
     * @return
     */
    public int getTimeAccuracy() {
        return getInt(8, this.body, this.byteOrder);
    }

    /**
     * The correction time in seconds between GMT (UTC) and the local timezone
     * of the following packet header timestamps. Examples: If the timestamps
     * are in GMT (UTC), thiszone is simply 0. If the timestamps are in Central
     * European time (Amsterdam, Berlin, ...) which is GMT + 1:00, thiszone must
     * be -3600. In practice, time stamps are always in GMT, so thiszone is
     * always 0.
     * 
     * @return
     */
    public long getTimeZoneCorrection() {
        return getUnsignedInt(4, this.body, this.byteOrder);
    }

    /**
     * the "snapshot length" for the capture (typically 65535 or even more, but
     * might be limited by the user)
     * 
     * @return
     */
    public long getSnapLength() {
        return getUnsignedInt(12, this.body, this.byteOrder);
    }

    public int getDataLinkType() {
        return getInt(16, this.body, this.byteOrder);
    }

    public static final int getUnsignedShort(final int offset, final byte[] buffer, final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (buffer[offset + 0] & 0xff) << 8 | buffer[offset + 1] & 0xff;
        }

        return (buffer[offset + 1] & 0xff) << 8 | buffer[offset + 0] & 0xff;
    }

    public static final long getUnsignedInt(final int offset, final byte[] buffer, final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (buffer[offset + 0] & 0xff) << 24 | (buffer[offset + 1] & 0xff) << 16
                    | (buffer[offset + 2] & 0xff) << 8 | buffer[offset + 3] & 0xff;
        }

        return (buffer[offset + 3] & 0xff) << 24 | (buffer[offset + 2] & 0xff) << 16
                | (buffer[offset + 1] & 0xff) << 8 | buffer[offset + 0] & 0xff;
    }

    public static final int getInt(final int offset, final byte[] buffer, final ByteOrder byteOrder) {
        return (int) getUnsignedInt(offset, buffer, byteOrder);

    }

    public static final PcapGlobalHeader parse(final Buffer in) throws IOException {
        final Buffer h = in.readBytes(4);
        final byte[] header = h.getArray();

        ByteOrder byteOrder = null;
        if (header[0] == MAGIC_BIG_ENDIAN[0] && header[1] == MAGIC_BIG_ENDIAN[1]
                && header[2] == MAGIC_BIG_ENDIAN[2] && header[3] == MAGIC_BIG_ENDIAN[3]) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (header[0] == MAGIC_LITTLE_ENDIAN[0] && header[1] == MAGIC_LITTLE_ENDIAN[1]
                && header[2] == MAGIC_LITTLE_ENDIAN[2] && header[3] == MAGIC_LITTLE_ENDIAN[3]) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            throw new IllegalArgumentException("Unknown header type");
        }

        final byte[] body = in.readBytes(20).getArray();

        return new PcapGlobalHeader(byteOrder, body);
    }

    /**
     * Will write this header to the output stream.
     * 
     * @param out
     */
    public void write(final OutputStream out) throws IOException {
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            out.write(MAGIC_BIG_ENDIAN);
        } else {
            out.write(MAGIC_LITTLE_ENDIAN);
        }
        out.write(this.body);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Version: ").append(getMajorVersion()).append(".").append(getMinorVersion()).append("\n");
        sb.append("TimeZone: ").append(getTimeZoneCorrection()).append("\n");
        sb.append("Accuracy: ").append(getTimeAccuracy()).append("\n");
        sb.append("SnapLength: ").append(getSnapLength()).append("\n");
        sb.append("Network: ").append(getDataLinkType()).append("\n");

        return sb.toString();
    }

    public static long unsignedInt(final byte a, final byte b, final byte c, final byte d) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (c & 0xff) << 8 | d & 0xff;
    }

}
