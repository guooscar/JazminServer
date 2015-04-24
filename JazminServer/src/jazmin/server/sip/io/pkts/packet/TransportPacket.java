/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

/**
 * Represents a Transport packet (Layer 4 in the OSI)
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TransportPacket extends IPPacket, Cloneable {

    int getSourcePort();

    void setSourcePort(int port);

    int getDestinationPort();

    void setDestinationPort(int port);

    boolean isUDP();

    boolean isTCP();


    @Override
    TransportPacket clone();

}
