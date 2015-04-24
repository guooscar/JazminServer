/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

/**
 * @author jonas@jonasborjesson.com
 */
public class FilterParseException extends FilterException {

    private final int errorOffset;

    /**
     * @param message
     */
    public FilterParseException(final int errorOffset, final String message) {
        super(message);
        this.errorOffset = errorOffset;
    }

    /**
     * @param cause
     */
    public FilterParseException(final int errorOffset, final Throwable cause) {
        super(cause);
        this.errorOffset = errorOffset;
    }

    /**
     * @param message
     * @param cause
     */
    public FilterParseException(final int errorOffset, final String message, final Throwable cause) {
        super(message, cause);
        this.errorOffset = errorOffset;
    }

    public int getErrorOffset() {
        return this.errorOffset;
    }

}
