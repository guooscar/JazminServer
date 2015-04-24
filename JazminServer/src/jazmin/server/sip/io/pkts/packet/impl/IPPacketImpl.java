/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.framer.TCPFramer;
import jazmin.server.sip.io.pkts.framer.UDPFramer;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.MACPacket;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class IPPacketImpl extends AbstractPacket implements IPPacket {

    private static final UDPFramer udpFramer = new UDPFramer();

    private static final TCPFramer tcpFramer = new TCPFramer();

    private final MACPacket parent;

    private final Buffer headers;

    private final int options;

    /**
     * 
     */
    public IPPacketImpl(final MACPacket parent, final Buffer headers, final int options, final Buffer payload) {
        super(Protocol.IPv4, parent, payload);
        assert parent != null;
        assert headers != null;
        this.parent = parent;
        this.headers = headers;
        this.options = options;
    }

    @Override
    public int getIpChecksum() {
        return this.headers.getUnsignedShort(10);
    }

    /**
     * Algorithm adopted from RFC 1071 - Computing the Internet Checksum
     * 
     * @return
     */
    private int calculateChecksum() {
        long sum = 0;
        for (int i = 0; i < this.headers.capacity() - 1; i += 2) {
            if (i != 10) {
                sum += this.headers.getUnsignedShort(i);
            }
        }

        while (sum >> 16 != 0) {
            sum = (sum & 0xffff) + (sum >> 16);
        }

        return (int) ~sum & 0xFFFF;
    }

    /**
     * Get the raw source ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawSourceIp() {
        return this.headers.getInt(12);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        final short a = this.headers.getUnsignedByte(12);
        final short b = this.headers.getUnsignedByte(13);
        final short c = this.headers.getUnsignedByte(14);
        final short d = this.headers.getUnsignedByte(15);
        return a + "." + b + "." + c + "." + d;
    }

    /**
     * Get the raw destination ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawDestinationIp() {
        return this.headers.getInt(16);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        final short a = this.headers.getUnsignedByte(16);
        final short b = this.headers.getUnsignedByte(17);
        final short c = this.headers.getUnsignedByte(18);
        final short d = this.headers.getUnsignedByte(19);
        return a + "." + b + "." + c + "." + d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to do for ip packets
    }

    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        // Note, you need to set the total length before you merge the packets since
        // Buffers.wrap will copy the bytes.
        final int size = this.headers.getReadableBytes() + (payload != null ? payload.getReadableBytes() : 0);
        this.setTotalLength(size);
        reCalculateChecksum();
        final Buffer pkt = Buffers.wrap(this.headers, payload);
        this.parent.write(out, pkt);
    }

    @Override
    public int getTotalIPLength() {
        // byte 3 - 4
        return this.headers.getUnsignedShort(2);
    }

    public void setTotalLength(final int length) {
        this.headers.setUnsignedShort(2, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceMacAddress(final String macAddress) {
        this.parent.setSourceMacAddress(macAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationMacAddress(final String macAddress) {
        this.parent.setDestinationMacAddress(macAddress);
    }

    @Override
    public void setSourceIP(final int a, final int b, final int c, final int d) {
        this.headers.setByte(12, (byte) a);
        this.headers.setByte(13, (byte) b);
        this.headers.setByte(14, (byte) c);
        this.headers.setByte(15, (byte) d);
        reCalculateChecksum();
    }

    @Override
    public void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.headers.setByte(12, a);
        this.headers.setByte(13, b);
        this.headers.setByte(14, c);
        this.headers.setByte(15, d);
    }

    @Override
    public void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.headers.setByte(16, (byte) a);
        this.headers.setByte(17, (byte) b);
        this.headers.setByte(18, (byte) c);
        this.headers.setByte(19, (byte) d);
        reCalculateChecksum();
    }

    @Override
    public void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.headers.setByte(16, a);
        this.headers.setByte(17, b);
        this.headers.setByte(18, c);
        this.headers.setByte(19, d);
    }

    @Override
    public void setSourceIP(final String sourceIp) {
        setIP(12, sourceIp);
    }

    @Override
    public void setDestinationIP(final String destinationIP) {
        setIP(16, destinationIP);
    }

    /**
     * Very naive initial implementation. Should be changed to do a better job
     * and its performance probably can go up a lot as well.
     * 
     * @param startIndex
     * @param address
     */
    private void setIP(final int startIndex, final String address) {
        final String[] parts = address.split("\\.");
        this.headers.setByte(startIndex + 0, (byte) Integer.parseInt(parts[0]));
        this.headers.setByte(startIndex + 1, (byte) Integer.parseInt(parts[1]));
        this.headers.setByte(startIndex + 2, (byte) Integer.parseInt(parts[2]));
        this.headers.setByte(startIndex + 3, (byte) Integer.parseInt(parts[3]));
        reCalculateChecksum();
    }

    /**
     * Whenever we change a value in the IP packet we need to update the
     * checksum as well.
     */
    @Override
    public void reCalculateChecksum() {
        final int checksum = calculateChecksum();
        this.headers.setUnsignedShort(10, checksum);
    }

    @Override
    public boolean verifyIpChecksum() {
        return calculateChecksum() == getIpChecksum();
    }

    @Override
    public IPPacket clone() {
        final MACPacket mac = this.parent.clone();
        final IPPacket pkt = new IPPacketImpl(mac, this.headers.clone(), this.options, getPayload().clone());
        return pkt;
    }

    @Override
    public long getTotalLength() {
        return this.parent.getTotalLength();
    }

    @Override
    public long getCapturedLength() {
        return this.parent.getCapturedLength();
    }

    @Override
    public Packet getNextPacket() throws IOException {
        final Buffer payload = getPayload();
        if (payload == null) {
            return null;
        }

        // the protocol is in byte 10
        final byte code = this.headers.getByte(9);
        final Protocol protocol = Protocol.valueOf(code);
        switch (protocol) {
        case UDP:
            return udpFramer.frame(this, payload);
        case TCP:
            return tcpFramer.frame(this, payload);
        default:
            throw new RuntimeException("Unknown Protocol. Was this SCTP or something???");
        }

    }

    /**
     * The version of this ip frame, will always be 4
     * 
     * @return
     */
    @Override
    public int getVersion() {
        return 4;
    }

    /**
     * The length of the ipv4 headers
     * 
     * @return
     */
    @Override
    public int getHeaderLength() {
        try {
            final byte b = this.headers.getByte(0);
            return b & 0x0F;
        } catch (final IOException e) {
            throw new RuntimeException("unable to get the header length of the IP packet due to IOException", e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean isFragmented() {
        return isMoreFragmentsSet() || getFragmentOffset() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReservedFlagSet() {
        try {
            final byte b = this.headers.getByte(6);
            return (b & 0x80) == 0x80;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDontFragmentSet() {
        try {
            final byte b = this.headers.getByte(6);
            return (b & 0x40) == 0x40;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreFragmentsSet() {
        try {
            final byte b = this.headers.getByte(6);
            return (b & 0x20) == 0x20;
        } catch (final IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public short getFragmentOffset() {
        try {
            final byte a = this.headers.getByte(6);
            final byte b = this.headers.getByte(7);
            return (short) ((a & 0x1F) << 8 | b & 0xFF);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getIdentification() {
        return this.headers.getUnsignedShort(4);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IPv4 ");
        sb.append(" Total Length: ").append(getTotalLength());
        sb.append(" ID: ").append(getIdentification());
        sb.append(" DF: ").append(isDontFragmentSet() ? "Set" : "Not Set");
        sb.append(" MF: ").append(isMoreFragmentsSet() ? "Set" : "Not Set");
        sb.append(" Fragment Offset: ").append(getFragmentOffset());

        return sb.toString();
    }

}
