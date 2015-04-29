/**
 * 
 */
package jazmin.server.sip.io.sip;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public SipException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public SipException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public SipException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public SipException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
