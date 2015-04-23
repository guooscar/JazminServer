/**
 * 
 */
package jazmin.server.sip.stack;

/**
 * Exception indicating that the maximum allowed size of a SIP message (or a
 * section thereof) has been reached. This typically leads to the message being
 * dropped on the floor and the connection dropped. Mainly used for protection
 * against attacks.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class MaxMessageSizeExceededException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public MaxMessageSizeExceededException(final String message) {
        super(message);
    }
}
