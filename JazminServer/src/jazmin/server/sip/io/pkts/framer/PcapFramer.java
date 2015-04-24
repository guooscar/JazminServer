/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;
import java.nio.ByteOrder;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.frame.PcapGlobalHeader;
import jazmin.server.sip.io.pkts.frame.PcapRecordHeader;
import jazmin.server.sip.io.pkts.packet.PCapPacket;
import jazmin.server.sip.io.pkts.packet.impl.PCapPacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class PcapFramer implements Framer<PCapPacket> {

    private final PcapGlobalHeader globalHeader;
    private final FramerManager framerManager;
    private final ByteOrder byteOrder;

    /**
     * 
     */
    public PcapFramer(final PcapGlobalHeader globalHeader, final FramerManager framerManager) {
        assert globalHeader != null;
        assert framerManager != null;

        this.globalHeader = globalHeader;
        this.byteOrder = this.globalHeader.getByteOrder();
        this.framerManager = framerManager;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.PCAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PCapPacket frame(final PCapPacket parent, final Buffer buffer) throws IOException {

        // note that for the PcapPacket the parent will always be null
        // so we are simply ignoring it.
        Buffer record = null;
        try {
            record = buffer.readBytes(16);
        } catch (final IndexOutOfBoundsException e) {
            // we def want to do something nicer than exit
            // on an exception like this. For now, good enough
            return null;
        }

        final PcapRecordHeader header = new PcapRecordHeader(this.byteOrder, record);
        final int length = (int) header.getCapturedLength();
        final Buffer payload = buffer.readBytes(length);

        return new PCapPacketImpl(header, payload);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
