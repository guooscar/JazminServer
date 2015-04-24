/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractApplicationPacket extends AbstractPacket implements ApplicationPacket {

    private final TransportPacket parent;

    private final Buffer payload;

    /**
     * @param p
     * @param parent
     * @param payload
     */
    public AbstractApplicationPacket(final Protocol p, final TransportPacket parent, final Buffer payload) {
        super(p, parent, payload);
        this.parent = parent;
        this.payload = payload;
    }

    protected TransportPacket getParent() {
        return this.parent;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getSourcePort() {
        return this.parent.getSourcePort();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getDestinationPort() {
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
     * 
     * {@inheritDoc}
     */
    @Override
    public final String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setSourceIP(final String sourceIp) {
        this.parent.setSourceIP(sourceIp);
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
     * 
     * {@inheritDoc}
     */
    @Override
    public final String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationIP(final String destinationIP) {
        this.parent.setDestinationIP(destinationIP);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final long getTotalLength() {
        return this.parent.getTotalLength();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setSourceMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setSourceMacAddress(macAddress);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setDestinationMacAddress(macAddress);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setSourcePort(final int port) {
        this.parent.setSourcePort(port);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationPort(final int port) {
        this.parent.setDestinationPort(port);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getTotalIPLength() {
        return this.parent.getTotalIPLength();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getVersion() {
        return this.parent.getVersion();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getHeaderLength() {
        return this.parent.getHeaderLength();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final int getIdentification() {
        return this.parent.getIdentification();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean isFragmented() {
        return this.parent.isFragmented();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean isReservedFlagSet() {
        return this.parent.isReservedFlagSet();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean isDontFragmentSet() {
        return this.parent.isDontFragmentSet();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean isMoreFragmentsSet() {
        return this.parent.isMoreFragmentsSet();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        final Buffer buffer = this.payload != null ? Buffers.wrap(this.payload, payload) : payload;
        this.parent.write(out, buffer);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public abstract ApplicationPacket clone();

}
