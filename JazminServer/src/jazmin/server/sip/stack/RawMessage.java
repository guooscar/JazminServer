/**
 * 
 */
package jazmin.server.sip.stack;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;

/**
 * Represents a raw sip message coming off of the network.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class RawMessage {

    public static final byte COLON = ':';

    public static final byte CR = '\r';

    public static final byte LF = '\n';

    public static final byte SP = ' ';

    public static final byte HTAB = '\t';

    /**
     * Every SIP message has an initial line, which is either a request line or
     * a response line. We don't care which but this is that one line...
     */
    private final byte[] initialLine;
    private int initalLineIndex = 0;

    /**
     * All the headers from the SIP message will be stored in this buffer.
     */
    private final byte[] headers;
    private int headersIndex = 0;

    /**
     * The payload of the SIP message.
     */
    private byte[] payload;
    private int payloadIndex = 0;

    /**
     * The content length as stated in the SIP message itself. For
     * connectionless protocols (such as UDP),the content length actually does
     * not have to be there (even though I haven't seen an implementation that
     * actually does this).
     * 
     * Negative 1 means that we haven't set it yet (unlike zero where we have
     * actually seen a content length header in the message)
     */
    private int contentLength = -1;

    /**
     * The maximum allowed content length.
     */
    private final int maxAllowedContentLength;

    private Byte lastByte = -1;

    private boolean crlf = false;

    private State state = State.INIT;

    private boolean done = false;
    /**
     * Used for detecting the content-length header
     */
    private final Buffer contentLengthBuffer = Buffers.wrap("Content-Length");
    int contentLengthIndex = 1; // because we will trigger on 'C'

    public enum State {
        /**
         * In the INIT state we will consume any CRLF we find. This is allowed
         * for stream based protocols according to RFC 3261.
         */
        INIT,
        /**
         * While in this state, we will consume the initial line in the SIP
         * message. We will not verify it is correct that is up to later stages
         * of the processing.
         */
        GET_INITIAL_LINE,

        /**
         * Consume all headers. Apart from the Content-Length header we will in
         * this state just seek to find the double CRLF indicating the
         * separation between headers and potentially a body. The headers are
         * merely just framed at this stage.
         */
        GET_HEADERS,

        /**
         * Once we have found the separator between the headers and the payload
         * we will just blindly a byte at a time until we have read the entire
         * payload. The length of the payload was discovered during the
         * GET_HEADERS phase where we were not only just consuming all headers
         * but we were looking for the Content-Length header as well.
         */
        GET_PAYLOAD,

        /**
         * Whenever we encounter something that potentially could be the start
         * of the Content-Length header we will enter this state. While in this
         * state, we will see if the next byte is the next expected byte in the
         * token "Content-Length" (for the long version of the header name). If
         * the next byte isn't what we expect, such in the case of e.g. Call-ID
         * where we would bail on 'a' or Content-Type where we would eventually
         * bail on the 'T', then we would once again move over to the state
         * {@link #GET_HEADERS}.
         */
        IS_CONTENT_LENGTH_HEADER, CONSUME_COLON, CONSUME_LWS, EXPECT_COLON, CONSUME_DIGIT;
    }

    /**
     * Creates a new holder for a raw SIP message.
     * 
     * @param maxAllowedInitialLineSize
     *            the maximum allowed size (in bytes) of the initial line.
     * @param maxAllowedHeaderSize
     *            the maximum allowed size (in bytes) of all the headers
     *            combined.
     * @param maxAllowedContentLength
     *            the maximum allowed size (in bytes) of the payload of the
     *            message.
     */
    public RawMessage(final int maxAllowedInitialLineSize, final int maxAllowedHeaderSize,
            final int maxAllowedContentLength) {
        this.initialLine = new byte[maxAllowedInitialLineSize];
        this.headers = new byte[maxAllowedHeaderSize];
        this.maxAllowedContentLength = maxAllowedContentLength;
    }

    private boolean isCRLF(final byte b) {
        if (this.lastByte == CR && b == LF) {
            this.crlf = true;
            return true;
        }
        return false;
    }

    private boolean doubleCRLF(final byte b) {
        return this.crlf && isCRLF(b);
    }

    public void write(final byte b) throws MaxMessageSizeExceededException, IOException {

        if (this.state == State.INIT) {
            if (b == CR || isCRLF(b)) {
                // ignore
            } else {
                this.state = State.GET_INITIAL_LINE;
                this.crlf = false;
                writeToInitialLine(b);
            }
        } else if (this.state == State.GET_INITIAL_LINE) {
            if (isCRLF(b)) {
                this.state = State.GET_HEADERS;
            } else if (b == CR) {
                this.crlf = false;
                // ignore
            } else {
                this.crlf = false;
                writeToInitialLine(b);
            }
        } else if (this.state == State.GET_HEADERS) {
            writeToHeaders(b);
            if (doubleCRLF(b)) {
                 if (getContentLength() > 0) {
                    this.state = State.GET_PAYLOAD;
                } else {
                    this.state = State.GET_PAYLOAD;
                    this.done = true;
                }
            }else if ( isCRLF(b)){
            	// ignore
            }else if (b == CR) {
                // ignore
            } else if (b == 'C') {
                this.state = State.IS_CONTENT_LENGTH_HEADER;
            } else {
                this.crlf = false;
            }
        } else if (this.state == State.IS_CONTENT_LENGTH_HEADER) {
            writeToHeaders(b);
            if (this.contentLengthBuffer.getByte(this.contentLengthIndex++) != b) {
                this.contentLengthIndex = 1;
                this.state = State.GET_HEADERS;
            } else if (this.contentLengthIndex == this.contentLengthBuffer.capacity()) {
                this.contentLengthIndex = 1;
                this.state = State.CONSUME_COLON;
            }
        } else if (this.state == State.CONSUME_COLON) {
            writeToHeaders(b);
            if (b == SP || b == HTAB) {
                // consume
            } else if (b == COLON) {
                this.state = State.CONSUME_DIGIT;
            } else {
                throw new RuntimeException("Expected HCOLON got " + Character.toString((char) b));
            }
        } else if (this.state == State.CONSUME_DIGIT) {
            writeToHeaders(b);
            if (this.contentLength == -1 && (b == SP || b == HTAB)) {
                // consume
            } else if (isDigit(b)) {
                if (this.contentLength == -1) {
                    this.contentLength = 0;
                }
                this.contentLength = this.contentLength * 10 + b - 48;
            } else {
                setContentLength(this.contentLength);
                this.crlf = false;
                this.state = State.GET_HEADERS;
            }
        } else if (this.state == State.GET_PAYLOAD) {
            writeToPayload(b);
            if (payloadCompleted()) {
                this.done = true;
            }
        } else {
            throw new RuntimeException("Unknown state " + this.state);
        }

        this.lastByte = b;
    }

    public static boolean isDigit(final char ch) {
        return ch >= 48 && ch <= 57;
    }

    public static boolean isDigit(final byte b) {
        return isDigit((char) b);
    }

    public boolean isComplete() {
        return this.done;
    }

    /**
     * Get the initial line of this raw message.
     * 
     * @return
     */
    public Buffer getInitialLine() {
        return Buffers.wrap(this.initialLine, 0, this.initalLineIndex);
    }

    /**
     * Write a byte to the initial line. if the operation succeeds, true will be
     * returned but if we hit the maximum allocated length of the initial line,
     * then we will return false. The reason for this upper boundary is that you
     * don't want an attacker to have you look for the initial line forever so
     * once we hit our maximum allowed size of the initial line we will give up
     * and the connection should be dropped.
     * 
     * @param b
     * @return
     */
    public void writeToInitialLine(final byte b) throws MaxMessageSizeExceededException {
        try {
            this.initialLine[this.initalLineIndex++] = b;
        } catch (final IndexOutOfBoundsException e) {
            throw new MaxMessageSizeExceededException("Maximum initial line exceeded");
        }
    }

    /**
     * Get all the raw headers of this message
     * 
     * @return
     */
    public Buffer getHeaders() {
        return Buffers.wrap(this.headers, 0, this.headersIndex);
    }

    /**
     * Write a byte to the header section of this raw message.
     * 
     * @param b
     *            the byte to write
     * @return true if there were still space in the header section, false
     *         otherwise
     */
    public void writeToHeaders(final byte b) throws MaxMessageSizeExceededException {
        try {
            this.headers[this.headersIndex++] = b;
        } catch (final IndexOutOfBoundsException e) {
            throw new MaxMessageSizeExceededException("Maximum allowed header size exceeded");
        }
    }

    /**
     * Get the payload of this raw message.
     * 
     * @return the payload or null if there is no payload.
     */
    public Buffer getPayload() {
        if (this.payloadIndex == 0) {
            return null;
        }
        return Buffers.wrap(this.payload);
    }

    /**
     * Write the next byte to the payload section of this message.
     * 
     * @param b
     * @throws MaxMessageSizeExceededException
     */
    public void writeToPayload(final byte b) throws MaxMessageSizeExceededException {
        try {
            this.payload[this.payloadIndex++] = b;
        } catch (final IndexOutOfBoundsException e) {
            throw new MaxMessageSizeExceededException("Maximum allowed payload size exceeded");
        }
    }

    /**
     * For stream based protocols there could be another message coming directly
     * after this one so you just cant read until the {@link ChannelBuffer} runs
     * out of bytes. Therefore, you must check how much you have read "manually"
     * and based on that decide whether you are done with processing the payload
     * of this message or not.
     * 
     * @return
     */
    public int getPayloadWriteIndex() {
        // return this.payload.getWriterIndex();
        return this.payloadIndex;
    }

    /**
     * Check whether we have read the entire payload.
     * 
     * @return
     */
    public boolean payloadCompleted() {
        return this.payloadIndex == this.contentLength;
        // return !this.payload.hasWritableBytes();
    }

    /**
     * Get the content length of this message. A return value of -1 (negative 1)
     * means that we do not know how big the payload of the message is. This
     * also means that we still haven't located the Content-Length header within
     * the message (if there is one).
     * 
     * @return
     */
    public int getContentLength() {
        return this.contentLength;
    }

    /**
     * Set the expected content length of this message. This value comes from
     * the Content-Length header and if it is exceeding the maximum allowed
     * payload length then we will throw a
     * {@link MaxMessageSizeExceededException}.
     * 
     * @param contentLength
     *            the content length of the payload
     * @throws MaxMessageSizeExceededException
     *             in case the payload exceeds the maximum allowed length.
     */
    public void setContentLength(final int contentLength) throws MaxMessageSizeExceededException {
        if (contentLength > this.maxAllowedContentLength) {
            throw new MaxMessageSizeExceededException("Content length exceeds the maximum allowed length");
        }
        this.contentLength = contentLength;
        this.payload = new byte[contentLength];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.initialLine.toString());
        sb.append(this.headers.toString());
        sb.append(this.payload.toString());
        return sb.toString();
    }

}
