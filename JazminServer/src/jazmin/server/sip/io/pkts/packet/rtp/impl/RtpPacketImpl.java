/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.rtp.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.impl.AbstractPacket;
import jazmin.server.sip.io.pkts.packet.rtp.RtpPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RtpPacketImpl extends AbstractPacket implements RtpPacket {

    private final TransportPacket parent;

    /**
     * All the RTP headers as one buffer.
     */
    private final Buffer headers;

    /**
     * The raw payload of the RTP packet. Is most likely audio or video.
     */
    private final Buffer payload;

    /**
     * 
     */
    public RtpPacketImpl(final TransportPacket parent, final Buffer headers, final Buffer payload) {
        super(Protocol.RTP, parent, payload);
        this.parent = parent;
        this.headers = headers;
        this.payload = payload;
    }

    @Override
    public int getVersion() {
        try {
            return (this.headers.getByte(0) & 0xC0) >> 6;
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to parse out the RTP version, not enough data", e);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to parse out the RTP version, IOException when trying.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPadding() throws IOException {
        return (this.headers.getByte(0) & 0x20) == 0x020;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExtensions() throws IOException {
        return (this.headers.getByte(0) & 0x10) == 0x010;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMarker() throws IOException {
        return (this.headers.getByte(1) & 0xff & 0x80) == 0x80;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPayloadType() throws IOException {
        return this.headers.getByte(1) & 0xff & 0x7f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeqNumber() throws IOException {
        // TODO: this is not quite right...
        return this.headers.getShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() throws IOException {
        return (long) (this.headers.getByte(4) & 0xff) << 24 | (long) (this.headers.getByte(5) & 0xff) << 16
                | (long) (this.headers.getByte(6) & 0xff) << 8 | this.headers.getByte(7) & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSyncronizationSource() throws IOException {
        return (long) (this.headers.getByte(8) & 0xff) << 24 | (long) (this.headers.getByte(9) & 0xff) << 16
                | (long) (this.headers.getByte(10) & 0xff) << 8 | this.headers.getByte(11) & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContributingSource() throws IOException {
        return this.headers.getByte(0) & 0x0F;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSourcePort() {
        return this.parent.getSourcePort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDestinationPort() {
        return this.parent.getDestinationPort();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getRawSourceIp() {
        return this.parent.getRawSourceIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getRawDestinationIp() {
        return this.parent.getRawDestinationIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceIP(final String sourceIp) {
        this.parent.setSourceIP(sourceIp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationIP(final String destinationIP) {
        this.parent.setDestinationIP(destinationIP);
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("Seq=").append(getSeqNumber());
            sb.append(" type=").append(getPayloadType());
            sb.append(" src=").append(getSourceIP()).append(":").append(getSourcePort());
            sb.append(" dst=").append(getDestinationIP()).append(":").append(getDestinationPort());
            return sb.toString();
        } catch (final IOException e) {
            return super.toString();
        }

    }

    @Override
    public byte[] dumpPacket() {
        final int headerLength = this.headers.capacity();
        final int payloadLength = this.payload.capacity();

        final byte[] dump = new byte[headerLength + payloadLength];
        System.arraycopy(this.headers.getArray(), 0, dump, 0, headerLength);
        System.arraycopy(this.payload.getArray(), 0, dump, headerLength, payloadLength);
        return dump;
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        throw new RuntimeException("Sorry, not implemented just yet.");
    }

    @Override
    public long getTotalLength() {
        return this.parent.getTotalLength();
    }

    @Override
    public int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    @Override
    public void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    @Override
    public void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    @Override
    public void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    @Override
    public boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    @Override
    public RtpPacket clone() {
        throw new RuntimeException("Sorry, not implemented just yet");
    }

    @Override
    public void setSourcePort(final int port) {
        this.parent.setSourcePort(port);
    }

    @Override
    public void setDestinationPort(final int port) {
        this.parent.setDestinationPort(port);
    }

    @Override
    public int getTotalIPLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHeaderLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIdentification() {
        return this.parent.getIdentification();
    }

    @Override
    public boolean isFragmented() {
        return this.parent.isFragmented();
    }

    @Override
    public boolean isReservedFlagSet() {
        return this.parent.isReservedFlagSet();
    }

    @Override
    public boolean isDontFragmentSet() {
        return this.parent.isDontFragmentSet();
    }

    @Override
    public boolean isMoreFragmentsSet() {
        return this.parent.isMoreFragmentsSet();
    }

    @Override
    public short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

    @Override
    public long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Packet getNextPacket() throws IOException {
        // no more packets for RTP
        return null;
    }

    @Override
    public boolean isUDP() {
        return this.parent.isUDP();
    }

    @Override
    public boolean isTCP() {
        return this.parent.isTCP();
    }

}
