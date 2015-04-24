/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp;

/**
 * @author jonas@jonasborjesson.com
 */
public class SdpException extends Exception {

    public SdpException() {
        super();
    }

    public SdpException(final String message) {
        super(message);
    }

    public SdpException(final String message, final Throwable rootCause) {
        super(message, rootCause);
    }

    public SdpException(final Throwable rootCause) {
        super(rootCause);
    }

    public Throwable getRootCause() {
        return fillInStackTrace();
    }

}
