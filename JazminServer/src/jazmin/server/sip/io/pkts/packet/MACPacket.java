/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

/**
 * Represents a packet from the Data Link Layer (DLL - Layer 2 in the OSI
 * model). Now, this is not 100% accurate since the MAC layer is really a sub
 * layer of DLL but whatever, it works for now.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface MACPacket extends PCapPacket, Cloneable {

    String getSourceMacAddress();

    /**
     * Set the MAC address of this {@link MACPacket}.
     * 
     * @param macAddress
     * @throws IllegalArgumentException
     *             in case the MAC address specified is null or the empty
     *             string.
     */
    void setSourceMacAddress(String macAddress) throws IllegalArgumentException;

    String getDestinationMacAddress();

    void setDestinationMacAddress(String macAddress) throws IllegalArgumentException;

    @Override
    MACPacket clone();

}
