/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp;

/**
 * More or less copied directly from the javax.sdp reference implementation so
 * all credit goes to those guys.
 * 
 * @author deruelle
 */
public class SdpParseException extends SdpException {

    private final int lineNumber;
    private final int charOffset;

    /**
     * Constructs a new SdpParseException when the parser needs to throw an
     * exception indicating a parsing failure.
     * 
     * @param lineNumber
     *            SDP line number that caused the exception.
     * @param charOffset
     *            offset of the characeter that caused the exception.
     * @param message
     *            a String containing the text of the exception message
     * @param rootCause
     *            the Throwable exception that interfered with the Codelet's
     *            normal operation, making this Codelet exception necessary.
     */
    public SdpParseException(final int lineNumber, final int charOffset, final String message, final Throwable rootCause) {
        super(message, rootCause);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    /**
     * Constructs a new SdpParseException when the parser needs to throw an
     * exception indicating a parsing failure.
     * 
     * @param lineNumber
     *            SDP line number that caused the exception.
     * @param charOffset
     *            offset of the characeter that caused the exception.
     * @param message
     *            a String containing the text of the exception message
     */
    public SdpParseException(final int lineNumber, final int charOffset, final String message) {
        super(message);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    /**
     * Returns the line number where the error occured
     * 
     * @return the line number where the error occured
     */
    public int getLineNumber() {
        return this.lineNumber;
    }

    /**
     * Returns the char offset where the error occured.
     * 
     * @return the char offset where the error occured.
     */
    public int getCharOffset() {
        return this.charOffset;
    }

}
