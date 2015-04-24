/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

/**
 * @author jonas@jonasborjesson.com
 */
public class FilterException extends RuntimeException {

    /**
     * @param message
     */
    public FilterException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public FilterException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public FilterException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
