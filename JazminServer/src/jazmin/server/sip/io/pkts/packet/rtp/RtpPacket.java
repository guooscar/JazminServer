/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.rtp;

import java.io.IOException;

import jazmin.server.sip.io.pkts.packet.impl.ApplicationPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RtpPacket extends ApplicationPacket {

    @Override
    int getVersion();

    boolean hasPadding() throws IOException;

    boolean hasExtensions() throws IOException;

    boolean hasMarker() throws IOException;

    int getPayloadType() throws IOException;

    int getSeqNumber() throws IOException;

    long getTimestamp() throws IOException;

    long getSyncronizationSource() throws IOException;

    int getContributingSource() throws IOException;

    /**
     * Dump the entire {@link RtpPacket} as a raw byte-array.
     * 
     * @return
     */
    byte[] dumpPacket();

}
