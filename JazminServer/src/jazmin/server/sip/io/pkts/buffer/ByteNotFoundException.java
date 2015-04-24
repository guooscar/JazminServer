/**
 * 
 */
package jazmin.server.sip.io.pkts.buffer;

/**
 * Exception for readUntil-methods and the like
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class ByteNotFoundException extends BufferException {

    private static final long serialVersionUID = 1L;

    private final byte[] bytes;

    /**
     * 
     * @param b
     */
    public ByteNotFoundException(final byte b) {
        // TODO: convert to hex string as well
        super("Unable to locate byte " + b);
        this.bytes = new byte[] { b };
    }

    public ByteNotFoundException(final byte... bytes) {
        super("Unable to locate any of the bytes " + bytes);
        this.bytes = bytes;
    }

    public ByteNotFoundException(final int maxBytes, final byte... bytes) {
        super("Gave up looking after reading " + maxBytes + " bytes. You asked me to find any of the following bytes: "
                + bytes);
        this.bytes = bytes;
    }

    /**
     * The byte that the user search for but we couldn't find.
     * 
     * @return
     */
    public byte getByte() {
        return this.bytes[0];
    }

    /**
     * The bytes that the user search for but we couldn't find.
     * 
     * @return
     */
    public byte[] getBytes() {
        return this.bytes;
    }

}
