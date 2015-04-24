/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

import jazmin.server.sip.io.pkts.packet.impl.TransportPacketFactoryImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * You can't write java without a bunch of factories! :-)
 * 
 * @author jonas@jonasborjesson.com
 */
public final class PacketFactory {

    private static final PacketFactory instance = new PacketFactory();

    private static final TransportPacketFactory transportFactory = new TransportPacketFactoryImpl(instance);

    public static PacketFactory getInstance() {
        return instance;
    }

    /**
     * Private constructor
     */
    private PacketFactory() {
        // left empty intentionally
    }

    /**
     * Obtain a reference to the {@link TransportPacketFactory} through which
     * you can create any arbitrary {@link Protocol#UDP} and
     * {@link Protocol#TCP} packets.
     * 
     * @return
     */
    public TransportPacketFactory getTransportFactory() {
        return transportFactory;
    }

}
