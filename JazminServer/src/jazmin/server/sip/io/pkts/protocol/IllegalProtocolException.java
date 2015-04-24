/**
 * 
 */
package jazmin.server.sip.io.pkts.protocol;

/**
 * Signals that a {@link Protocol} was used in a context that was not allowed by
 * the API.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class IllegalProtocolException extends Exception {

    private final Protocol protocol;

    /**
     * 
     */
    public IllegalProtocolException(final Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @param message
     */
    public IllegalProtocolException(final Protocol protocol, final String message) {
        super(message);
        this.protocol = protocol;
    }

    /**
     * @param cause
     */
    public IllegalProtocolException(final Protocol protocol, final Throwable cause) {
        super(cause);
        this.protocol = protocol;
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalProtocolException(final Protocol protocol, final String message, final Throwable cause) {
        super(message, cause);
        this.protocol = protocol;
    }

    /**
     * The {@link Protocol} that the user tried to use in the wrong text and as
     * such this exception was generated.
     * 
     * @return
     */
    public Protocol getProtocol() {
        return this.protocol;
    }

}
