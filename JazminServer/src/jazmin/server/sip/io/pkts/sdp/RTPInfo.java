/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp;

/**
 * TODO: need to add the profile as well. voice, video, application etc.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RTPInfo {

    /**
     * Get the address to where we should be sending. Typically this is the
     * IP-address of the receiver.
     * 
     * @return
     */
    String getAddress();

    /**
     * Get the address as a raw byte array.
     * 
     * @return
     * @throws IllegalArgumentException in case the address is not an IPv4 address
     */
    byte[] getRawAddress() throws IllegalArgumentException;

    /**
     * Get the media port where we are expected to send media. Typical
     * 
     * @return
     */
    int getMediaPort();

}
