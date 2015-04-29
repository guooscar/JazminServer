/**
 * 
 */
package jazmin.server.sip.io.buffer;

/**
 * Base class for all exceptions thrown by a buffer
 * 
 * @author jonas@jonasborjesson.com
 */
@SuppressWarnings("serial")
public class BufferException extends RuntimeException {

    public BufferException() {
        super();
    }

    public BufferException(final String message) {
        super(message);
    }

    public BufferException(final Throwable cause) {
        super(cause);
    }

    public BufferException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
