/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.rtcp.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.impl.AbstractPacket;
import jazmin.server.sip.io.pkts.packet.rtcp.RtcpPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class RtcpPacketImpl extends AbstractPacket implements RtcpPacket {

    private final TransportPacket parent;

    public RtcpPacketImpl(final TransportPacket parent, final Buffer headers, final Buffer payload) {
        super(Protocol.RTCP, parent, payload);
        this.parent = parent;
        // this.headers = headers;
        // this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalIPLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIpChecksum() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reCalculateChecksum() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyIpChecksum() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Packet getNextPacket() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RtcpPacket clone() {
        // TODO Auto-generated method stub
        return null;
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
    public int getRawSourceIp() {
        return this.parent.getRawSourceIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        return this.parent.getDestinationIP();
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
    public void setDestinationPort(final int port) {
        this.parent.setDestinationPort(port);
    }

    @Override
    public void setSourcePort(final int port) {
        this.parent.setSourcePort(port);
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
    public void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    @Override
    public void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
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
