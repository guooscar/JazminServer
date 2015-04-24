package jazmin.server.sip.io.pkts;

import java.io.IOException;

import jazmin.server.sip.io.pkts.packet.Packet;

/**
 * 
 * Whenever there is a new packet being read off of the stream, the registered
 * {@link PacketHandler} will be called.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface PacketHandler {

    /**
     * Will be called by the {@link Pcap} class as soon as it detects a new
     * {@link Packet} in the pcap stream.
     * 
     * @param packet
     *            the new {@link Packet} as read off of the pcap stream.
     * @throws IOException
     */
    void nextPacket(Packet packet) throws IOException;

}
