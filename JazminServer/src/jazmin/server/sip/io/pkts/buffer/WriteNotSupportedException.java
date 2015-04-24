/**
 * 
 */
package jazmin.server.sip.io.pkts.buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public final class WriteNotSupportedException extends BufferException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public WriteNotSupportedException() {
    }

    /**
     * @param message
     */
    public WriteNotSupportedException(final String message) {
        super(message);
    }

}
