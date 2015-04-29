/**
 * 
 */
package jazmin.server.sip.io.sip;


/**
 * @author jonas@jonasborjesson.com
 */
public class SipParseException extends SipException {

    private static final long serialVersionUID = 7627471115511100108L;

    private final int errorOffset;

    public SipParseException(final int errorOffset, final String message) {
        super(message);
        this.errorOffset = errorOffset;
    }

    public SipParseException(final String message) {
        this(0, message);
    }

    public SipParseException(final int errorOffset, final String message, final Exception cause) {
        super(message, cause);
        this.errorOffset = errorOffset;
    }

    public int getErrorOffset() {
        return this.errorOffset;
    }

}
