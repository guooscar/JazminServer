/**
 * 
 */
package jazmin.server.sip.io.sip.impl;

import java.io.IOException;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.ByteNotFoundException;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class SipInitialLine extends SipParser {

    protected SipInitialLine() {
        // left empty intentionally
    }

    /**
     * The request initial line as a raw buffer.
     * 
     * @return
     */
    public abstract Buffer getBuffer();

    /**
     * Simple method to check whether the supplied buffer could be a SIP
     * response line. Hence, does it start with SIP/2.0 or not. Note, if this
     * method returns false it does not necessarily mean that this is a request
     * line but if you have already determined that that the incoming data could
     * be a SIP message (e.g. by using the method
     * {@link SipFramer#couldBeSipMessage(Buffer)}) then it is very likely that
     * it is a request line.
     * 
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static boolean isResponseLine(final Buffer buffer) throws IOException {
        if (buffer == null || buffer.getReadableBytes() < 7) {
            return false;
        }

        final byte a = buffer.getByte(0);
        final byte b = buffer.getByte(1);
        final byte c = buffer.getByte(2);
        final byte d = buffer.getByte(3);
        final byte e = buffer.getByte(4);
        final byte f = buffer.getByte(5);
        final byte g = buffer.getByte(6);
        return a == 'S' && b == 'I' && c == 'P' && d == '/' && e == '2' && f == '.' && g == '0';
    }

    /**
     * Parse the buffer into a SIP initial line, which either can be a
     * {@link SipRequestLine} or a {@link SipResponseLine}.
     * 
     * The parsing will only check so that a few things are correct but in
     * general it doesn't do any deeper analysis of the initial line. To make
     * sure that the resulting sip message actually is correct, call the
     * {@link SipMessage#verify()} method, which will do a deeper analysis of
     * the sip message
     * 
     * @param buffer
     * @return
     */
    public static final SipInitialLine parse(final Buffer buffer) throws SipParseException {
        Buffer part1 = null;
        Buffer part2 = null;
        Buffer part3 = null;
        try {
            part1 = buffer.readUntil(SipParser.SP);
            part2 = buffer.readUntil(SipParser.SP);
            part3 = buffer.readLine();

            if (SipParser.SIP2_0.equals(part1)) {
                final int statusCode = Integer.parseInt(part2.toString());
                return new SipResponseLine(statusCode, part3);
            }

            // not a response so then the last part must be the SIP/2.0
            // otherwise this is not a valid SIP initial line
            expectSIP2_0(part3);

            return new SipRequestLine(part1, part2);

        } catch (final NumberFormatException e) {
            final int index = buffer.getReaderIndex() - part3.capacity() - part2.capacity() - 1;
            throw new SipParseException(index, "unable to parse the SIP response code as an integer");
        } catch (final ByteNotFoundException e) {
            throw new SipParseException(buffer.getReaderIndex(), "expected space");
        } catch (final SipParseException e) {
            // is only thrown by the expectSIP2_0. Calculate the correct
            // index into the buffer
            final int index = buffer.getReaderIndex() - part3.capacity() + e.getErrorOffset() - 1;
            throw new SipParseException(index, "Wrong SIP version");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "could not read from stream", e);
        }
    }

    public SipRequestLine toRequestLine() {
        throw new ClassCastException("Cannot cast object to " + SipRequestLine.class);
    }

    public SipResponseLine toResponseLine() {
        throw new ClassCastException("Cannot cast object to " + SipResponseLine.class);
    }

    public boolean isResponseLine() {
        return false;
    }

    public boolean isRequestLine() {
        return false;
    }

    /**
     * Write the bytes representing this {@link SipInitialLine} into the
     * destination {@link Buffer}.
     * 
     * @param dst
     */
    public abstract void getBytes(Buffer dst);

    @Override
    protected abstract SipInitialLine clone();

}
