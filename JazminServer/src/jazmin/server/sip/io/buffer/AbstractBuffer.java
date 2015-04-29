/**
 * 
 */
package jazmin.server.sip.io.buffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBuffer implements Buffer {

    private static final byte LF = '\n';
    private static final byte CR = '\r';

    /**
     * From where we will continue reading
     */
    protected int readerIndex;

    /**
     * This is where we will write the next byte.
     */
    protected int writerIndex;

    /**
     * The position of the reader index that has been marked. I.e., this is the
     * position we will move the reader index back to if someone is asking us to
     * {@link #resetReaderIndex()}
     */
    protected int markedReaderIndex;

    /**
     * We will pretend that any bytes below this boundary doesn't exist.
     */
    protected int lowerBoundary;

    /**
     * Any bytes above this boundary is not accessible to us
     */
    protected int upperBoundary;

    // protected AbstractBuffer(final int readerIndex, final int lowerBoundary,
    // final int upperBoundary,
    // final byte[] buffer) {
    protected AbstractBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
            final int writerIndex) {
        assert lowerBoundary <= upperBoundary;
        this.readerIndex = readerIndex;
        this.markedReaderIndex = readerIndex;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
        this.writerIndex = writerIndex;
    }

    @Override
    public abstract Buffer clone();

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReaderIndex() {
        return this.readerIndex;
    }

    @Override
    public int getWriterIndex() {
        return this.writerIndex;
    }

    @Override
    public void setReaderIndex(final int index) {
        this.readerIndex = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {
        return this.upperBoundary - this.lowerBoundary;
    }

    @Override
    public int getWritableBytes() {
        return this.upperBoundary - this.writerIndex;
    }

    @Override
    public boolean hasWritableBytes() {
        return getWritableBytes() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markReaderIndex() {
        this.markedReaderIndex = this.readerIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int stop) {
        return this.slice(getReaderIndex(), stop);
    }

    @Override
    public Buffer slice() {
        if (!hasReadableBytes()) {
            return Buffers.EMPTY_BUFFER;
        }
        return this.slice(getReaderIndex(), getWriterIndex() - this.lowerBoundary);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getReadableBytes() {
        return this.writerIndex - this.readerIndex - this.lowerBoundary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetReaderIndex() {
        this.readerIndex = this.markedReaderIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readUntil(final byte b) throws IOException, ByteNotFoundException {
        return this.readUntil(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
            IllegalArgumentException {
        final int index = indexOf(maxBytes, bytes);
        if (index == -1) {
            throw new ByteNotFoundException(bytes);
        }

        final int size = index - getReaderIndex();
        Buffer result = null;
        if (size == 0) {
            result = Buffers.EMPTY_BUFFER;
        } else {
            result = readBytes(size);
        }
        readByte(); // consume the one at the index as well
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final byte b) throws IOException, ByteNotFoundException, IllegalArgumentException {
        return this.indexOf(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
            IllegalArgumentException {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified. Not sure what you want me to look for");
        }

        final int start = getReaderIndex();
        int index = -1;

        while (hasReadableBytes() && getReaderIndex() - start < maxBytes && index == -1) {
            if (isByteInArray(readByte(), bytes)) {
                index = this.readerIndex - 1;
            }
        }

        this.readerIndex = start;

        if (getReaderIndex() - start >= maxBytes) {
            throw new ByteNotFoundException(maxBytes, bytes);
        }

        return index;
    }

    private boolean isByteInArray(final byte b, final byte[] bytes) {
        for (final byte x : bytes) {
            if (x == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readLine() throws IOException {
        final int start = this.readerIndex;
        boolean foundCR = false;
        while (hasReadableBytes()) {
            final byte b = readByte();
            switch (b) {
            case LF:
                return slice(start, this.readerIndex - (foundCR ? 2 : 1));
            case CR:
                foundCR = true;
                break;
            default:
                if (foundCR) {
                    --this.readerIndex;
                    return slice(start, this.lowerBoundary + this.readerIndex - 1);
                }
            }
        }

        // i guess there were nothing for us to read
        if (start >= this.readerIndex) {
            return null;
        }

        return slice(start, this.readerIndex);
    }

    @Override
    public Buffer readUntilDoubleCRLF() throws IOException {
        final int start = this.readerIndex;
        int found = 0;
        while (found < 4 && hasReadableBytes()) {
            final byte b = readByte();
            if ((found == 0 || found == 2) && b == CR) {
                ++found;
            } else if ((found == 1 || found == 3) && b == LF) {
                ++found;
            } else {
                found = 0;
            }
        }
        if (found == 4) {
            return slice(start, this.readerIndex - 4);
        } else {
            this.readerIndex = start;
            return null;
        }
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     * 
     * @param length
     *            the length the user wishes to read
     * @throws IndexOutOfBoundsException
     *             in case we don't have the bytes available
     */
    protected void checkReadableBytes(final int length) throws IndexOutOfBoundsException {
        if (!checkReadableBytesSafe(length)) {
            throw new IndexOutOfBoundsException("Not enough readable bytes");
        }
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     * 
     * @param length
     *            the length the user wishes to read
     * @return true if we have enough bytes available for read
     */
    protected boolean checkReadableBytesSafe(final int length) {
        return getReadableBytes() >= length;
    }

    /**
     * Convenience method for checking if we can read at the index
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index >= this.lowerBoundary + capacity()) {
            //if (index >= this.lowerBoundary + this.writerIndex) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Check whether we have enough space for writing the desired length.
     * 
     * @param length
     * @return
     */
    protected boolean checkWritableBytesSafe(final int length) {
        return getWritableBytes() >= length;
    }

    /**
     * Convenience method for checking whether we can write at the specified
     * index.
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkWriterIndex(final int index) throws IndexOutOfBoundsException {
        if (index < this.writerIndex || index >= this.upperBoundary) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final short readUnsignedByte() throws IndexOutOfBoundsException, IOException {
        return (short) (readByte() & 0xFF);
    }

    /**
     * The underlying subclass should override this if it has write support.
     * {@inheritDoc}
     */
    @Override
    public boolean hasWriteSupport() {
        return false;
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
            UnsupportedEncodingException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
            WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

    @Override
    public int parseToInt() throws NumberFormatException, IOException {
        return parseToInt(10);
    }

    /**
     * (Copied from the Integer class and slightly altered to read from this
     * buffer instead of a String)
     * 
     * Parses the string argument as a signed integer in the radix specified by
     * the second argument. The characters in the string must all be digits of
     * the specified radix (as determined by whether
     * {@link java.lang.Character#digit(char, int)} returns a nonnegative
     * value), except that the first character may be an ASCII minus sign
     * <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative
     * value. The resulting integer value is returned.
     * <p>
     * An exception of type <code>NumberFormatException</code> is thrown if any
     * of the following situations occurs:
     * <ul>
     * <li>The first argument is <code>null</code> or is a string of length
     * zero.
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or larger than
     * {@link java.lang.Character#MAX_RADIX}.
     * <li>Any character of the string is not a digit of the specified radix,
     * except that the first character may be a minus sign <code>'-'</code> (
     * <code>'&#92;u002D'</code>) provided that the string is longer than length
     * 1.
     * <li>The value represented by the string is not a value of type
     * <code>int</code>.
     * </ul>
     * <p>
     * Examples: <blockquote>
     * 
     * <pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre>
     * 
     * </blockquote>
     * 
     * @param s
     *            the <code>String</code> containing the integer representation
     *            to be parsed
     * @param radix
     *            the radix to be used while parsing <code>s</code>.
     * @return the integer represented by the string argument in the specified
     *         radix.
     * @exception NumberFormatException
     *                if the <code>String</code> does not contain a parsable
     *                <code>int</code>.
     */
    @Override
    public int parseToInt(final int radix) throws NumberFormatException, IOException {
        if (getReadableBytes() == 0) {
            throw new NumberFormatException("Buffer is empty, cannot convert it to an integer");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = this.readerIndex;
        final int max = getReadableBytes() + this.readerIndex;
        int limit;
        int multmin;
        int digit;

        if (max > 0) {
            if (getByte(0) == (byte) '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) getByte(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + this + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + this + "\"");
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException("For input string: \"" + this + "\"");
            }
        } else {
            return -result;
        }
    }
}
