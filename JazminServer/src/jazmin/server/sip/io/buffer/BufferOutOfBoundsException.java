package jazmin.server.sip.io.buffer;

/**
 * 
 */

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class BufferOutOfBoundsException extends BufferException {

    private static final long serialVersionUID = 270368623884242630L;

    /**
     * The index where the error occurred
     */
    private final int index;

    /**
     * 
     */
    public BufferOutOfBoundsException(final int index) {
        super(index + " is out of bounds");
        this.index = index;
    }

    /**
     * @param message
     */
    public BufferOutOfBoundsException(final int index, final String message) {
        super(index + " is out of bounds" + (message != null ? " - " + message : ""));
        this.index = index;
    }

    /**
     * 
     * @return
     */
    public int getIndex() {
        return this.index;
    }

}
