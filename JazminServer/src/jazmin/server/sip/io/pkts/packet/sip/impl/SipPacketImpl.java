/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.impl.AbstractPacket;
import jazmin.server.sip.io.pkts.packet.impl.SDPPacketImpl;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.SipRequestPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipResponsePacket;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContentTypeHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.MaxForwardsHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RecordRouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.SipHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ToHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;
import jazmin.server.sip.io.pkts.protocol.Protocol;
import jazmin.server.sip.io.pkts.sdp.SDP;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class SipPacketImpl extends AbstractPacket implements SipPacket {

    private final TransportPacket parent;

    /**
     * The actual SIP message. The {@link SipPacket} is merely a thin wrapper
     * around this object in order to make if fit the pcap model whereas the
     * actual {@link SipMessage} is a pure SIP object only.
     */
    private final SipMessage msg;

    /**
     * 
     */
    public SipPacketImpl(final TransportPacket parent, final SipMessage msg) {
        super(Protocol.SIP, parent, null);
        this.parent = parent;
        this.msg = msg;
    }

    protected TransportPacket getTransportPacket() {
        return this.parent;
    }

    protected SipMessage getSipMessage() {
        return this.msg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.TransportPacket#getSourcePort()
     */
    @Override
    public int getSourcePort() {
        return this.parent.getSourcePort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.TransportPacket#getDestinationPort()
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

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getSourceIP()
     */
    @Override
    public String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(int, int, int, int)
     */
    @Override
    public void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(byte, byte, byte, byte)
     */
    @Override
    public void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(java.lang.String)
     */
    @Override
    public void setSourceIP(final String sourceIp) {
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

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getDestinationIP()
     */
    @Override
    public String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(int, int, int, int)
     */
    @Override
    public void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(byte, byte, byte, byte)
     */
    @Override
    public void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(java.lang.String)
     */
    @Override
    public void setDestinationIP(final String destinationIP) {
        this.parent.setDestinationIP(destinationIP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getTotalLength()
     */
    @Override
    public long getTotalLength() {
        return this.parent.getTotalLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getIpChecksum()
     */
    @Override
    public int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#reCalculateChecksum()
     */
    @Override
    public void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#verifyIpChecksum()
     */
    @Override
    public boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#getSourceMacAddress()
     */
    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#setSourceMacAddress(java.lang.String)
     */
    @Override
    public void setSourceMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setSourceMacAddress(macAddress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#getDestinationMacAddress()
     */
    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#setDestinationMacAddress(java.lang.String)
     */
    @Override
    public void setDestinationMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setDestinationMacAddress(macAddress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getArrivalTime()
     */
    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#write(java.io.OutputStream)
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        this.parent.write(out, Buffers.wrap(this.msg.toBuffer(), payload));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getInitialLine()
     */
    @Override
    public Buffer getInitialLine() {
        return this.msg.getInitialLine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toRequest()
     */
    @Override
    public SipRequestPacket toRequest() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toResponse()
     */
    @Override
    public SipResponsePacket toResponse() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipResponse");
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isResponse()
     */
    @Override
    public boolean isResponse() {
        return this.msg.isResponse();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isRequest()
     */
    @Override
    public boolean isRequest() {
        return this.msg.isRequest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContent()
     */
    @Override
    public Object getContent() throws SipParseException {
        return this.msg.getContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRawContent()
     */
    @Override
    public Buffer getRawContent() {
        return this.msg.getRawContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#hasContent()
     */
    @Override
    public boolean hasContent() {
        return this.msg.hasContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getMethod()
     */
    @Override
    public Buffer getMethod() throws SipParseException {
        return this.msg.getMethod();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getHeader(io.pkts.buffer.Buffer)
     */
    @Override
    public SipHeader getHeader(final Buffer headerName) throws SipParseException {
        return this.msg.getHeader(headerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getHeader(java.lang.String)
     */
    @Override
    public SipHeader getHeader(final String headerName) throws SipParseException {
        return this.msg.getHeader(headerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.pkts.packet.sip.SipPacket#addHeader(io.pkts.packet.sip.header.SipHeader
     * )
     */
    @Override
    public void addHeader(final SipHeader header) throws SipParseException {
        this.msg.addHeader(header);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.pkts.packet.sip.SipPacket#addHeaderFirst(io.pkts.packet.sip.header
     * .SipHeader)
     */
    @Override
    public void addHeaderFirst(final SipHeader header) throws SipParseException {
        this.msg.addHeaderFirst(header);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.pkts.packet.sip.SipPacket#setHeader(io.pkts.packet.sip.header.SipHeader
     * )
     */
    @Override
    public void setHeader(final SipHeader header) throws SipParseException {
        this.msg.setHeader(header);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getFromHeader()
     */
    @Override
    public FromHeader getFromHeader() throws SipParseException {
        return this.msg.getFromHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getToHeader()
     */
    @Override
    public ToHeader getToHeader() throws SipParseException {
        return this.msg.getToHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getViaHeader()
     */
    @Override
    public ViaHeader getViaHeader() throws SipParseException {
        return this.msg.getViaHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getViaHeaders()
     */
    @Override
    public List<ViaHeader> getViaHeaders() throws SipParseException {
        return this.msg.getViaHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getMaxForwards()
     */
    @Override
    public MaxForwardsHeader getMaxForwards() throws SipParseException {
        return this.msg.getMaxForwards();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRecordRouteHeader()
     */
    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipParseException {
        return this.msg.getRecordRouteHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRecordRouteHeaders()
     */
    @Override
    public List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException {
        return this.msg.getRecordRouteHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRouteHeader()
     */
    @Override
    public RouteHeader getRouteHeader() throws SipParseException {
        return this.msg.getRouteHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getRouteHeaders()
     */
    @Override
    public List<RouteHeader> getRouteHeaders() throws SipParseException {
        return this.msg.getRouteHeaders();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContactHeader()
     */
    @Override
    public ContactHeader getContactHeader() throws SipParseException {
        return this.msg.getContactHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getContentTypeHeader()
     */
    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        return this.msg.getContentTypeHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getCallIDHeader()
     */
    @Override
    public CallIdHeader getCallIDHeader() throws SipParseException {
        return this.msg.getCallIDHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#getCSeqHeader()
     */
    @Override
    public CSeqHeader getCSeqHeader() throws SipParseException {
        return this.msg.getCSeqHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInvite()
     */
    @Override
    public boolean isInvite() throws SipParseException {
        return this.msg.isInvite();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isBye()
     */
    @Override
    public boolean isBye() throws SipParseException {
        return this.msg.isBye();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isAck()
     */
    @Override
    public boolean isAck() throws SipParseException {
        return this.msg.isAck();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isOptions()
     */
    @Override
    public boolean isOptions() throws SipParseException {
        return this.msg.isOptions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isMessage()
     */
    @Override
    public boolean isMessage() throws SipParseException {
        return this.msg.isMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInfo()
     */
    @Override
    public boolean isInfo() throws SipParseException {
        return this.msg.isInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isCancel()
     */
    @Override
    public boolean isCancel() throws SipParseException {
        return this.msg.isCancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#isInitial()
     */
    @Override
    public boolean isInitial() throws SipParseException {
        return this.msg.isInitial();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#verify()
     */
    @Override
    public void verify() {
        this.msg.verify();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.sip.SipPacket#toBuffer()
     */
    @Override
    public Buffer toBuffer() {
        return this.msg.toBuffer();
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
    public abstract SipPacket clone();

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.PCapPacket#getCapturedLength()
     */
    @Override
    public long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getNextPacket()
     */
    @Override
    public Packet getNextPacket() throws IOException {
        try {
            final Object content = this.msg.getContent();
            if (content instanceof SDP) {
                return new SDPPacketImpl(this, (SDP) content);
            }
        } catch (final SipParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Buffer getPayload() {
        return this.msg.getRawContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getTotalIPLength()
     */
    @Override
    public int getTotalIPLength() {
        return this.parent.getTotalIPLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getVersion()
     */
    @Override
    public int getVersion() {
        return this.parent.getVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getHeaderLength()
     */
    @Override
    public int getHeaderLength() {
        return this.parent.getHeaderLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getIdentification()
     */
    @Override
    public int getIdentification() {
        return this.parent.getIdentification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isFragmented()
     */
    @Override
    public boolean isFragmented() {
        return this.parent.isFragmented();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isReservedFlagSet()
     */
    @Override
    public boolean isReservedFlagSet() {
        return this.parent.isReservedFlagSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isDontFragmentSet()
     */
    @Override
    public boolean isDontFragmentSet() {
        return this.parent.isDontFragmentSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isMoreFragmentsSet()
     */
    @Override
    public boolean isMoreFragmentsSet() {
        return this.parent.isMoreFragmentsSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getFragmentOffset()
     */
    @Override
    public short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (isUDP()) {
            sb.append("U ");
        } else if (isTCP()) {
            sb.append("T ");
        } else {
            // TODO: need WS, SCTP etc as well. but not as common
            // right now so no big deal.
        }

        // final DateTimeFormatter formatter =
        // DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS");
        final Instant timestamp = Instant.ofEpochMilli(getArrivalTime() / 1000);
        sb.append(timestamp.toString());
        sb.append(" ").append(getSourceIP()).append(":").append(getSourcePort());
        sb.append(" -> ").append(getDestinationIP()).append(":").append(getDestinationPort());
        sb.append("\n");
        sb.append(this.msg.toString());
        return sb.toString();
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
