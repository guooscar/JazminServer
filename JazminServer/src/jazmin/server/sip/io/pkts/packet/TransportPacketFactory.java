/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.protocol.IllegalProtocolException;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface TransportPacketFactory {

    /**
     * Create a new {@link TransportPacket}.
     * 
     * @param protocol
     *            which protocol, currently only {@link Protocol#UDP} and
     *            {@link Protocol#TCP} are supported
     * @param srcAddress
     *            the source address.
     * @param srcPort
     *            the source port
     * @param destAddress
     *            the destination address
     * @param destPort
     *            the destination port
     * @param payload
     *            the payload or null if none
     * @return a newly created {@link TransportPacket}
     * @throws IllegalArgumentException
     * @throws {@link IllegalProtocolException} in case any other protocol but
     *         {@link Protocol#UDP} or {@link Protocol#TCP} was specified.
     */
    TransportPacket create(Protocol protocol, String srcAddress, int srcPort, String destAddress, int destPort,
            Buffer payload) throws IllegalArgumentException, IllegalProtocolException;

    /**
     * Create a new {@link TransportPacket}.
     * 
     * @param protocol
     *            which protocol, currently only {@link Protocol#UDP} and
     *            {@link Protocol#TCP} are supported
     * @param srcAddress
     *            the source address.
     * @param srcPort
     *            the source port
     * @param destAddress
     *            the destination address
     * @param destPort
     *            the destination port
     * @param payload
     *            the payload or null if none
     * @return a newly created {@link TransportPacket}
     * @throws IllegalArgumentException
     * @throws {@link IllegalProtocolException} in case any other protocol but
     *         {@link Protocol#UDP} or {@link Protocol#TCP} was specified.
     */
    TransportPacket create(Protocol protocol, byte[] srcAddress, int srcPort, byte[] destAddress, int destPort,
            Buffer payload) throws IllegalArgumentException, IllegalProtocolException;

    /**
     * Create a {@link UDPPacket} with the specified payload and with the
     * following default values:
     * <ul>
     * <li>MAC Src Address: 00:00:00:00:00:00</li>
     * <li>MAC Dst Address: 00:00:00:00:00:00</li>
     * <li>IP Src Address: 127.0.0.1</li>
     * <li>IP Dst Address: 127.0.0.1</li>
     * <li>UDP Src Port: 0</li>
     * <li>UDP Dst Port: 0</li>
     * </ul>
     * 
     * @param payload
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalProtocolException
     */
    UDPPacket createUDP(long ts, Buffer payload) throws IllegalArgumentException, IllegalProtocolException;

    /**
     * @param payload
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalProtocolException
     */
    UDPPacket createUDP(Buffer payload) throws IllegalArgumentException, IllegalProtocolException;

}
