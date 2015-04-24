/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

/**
 * Signals that an error has been reached unexpectedly while parsing a packet.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class PacketParseException extends Exception {

    private static final long serialVersionUID = 3856475199594072886L;

    private final int errorOffset;

    /**
     * @param message
     */
    public PacketParseException(final int errorOffset, final String message) {
        super(message);
        this.errorOffset = errorOffset;
    }

    public PacketParseException(final int errorOffset, final String message, final Exception cause) {
        super(message, cause);
        this.errorOffset = errorOffset;
    }

    /**
     * Get the offset into the buffer where the error occurred.
     * 
     * @return
     */
    public int getErroOffset() {
        return this.errorOffset;
    }


}
