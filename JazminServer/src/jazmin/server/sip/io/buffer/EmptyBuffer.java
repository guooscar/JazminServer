/**
 * 
 */
package jazmin.server.sip.io.buffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Represents an empty buffer.
 * 
 * @author jonas@jonasborjesson.com
 */
public class EmptyBuffer implements Buffer {

    private static final byte[] EMPTY = new byte[0];

    /**
     * 
     */
    protected EmptyBuffer() {
        // only Buffers should create this one
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException, IOException {
        if (length == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException("Not enough readable bytes");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readLine() throws IOException {
        throw new IndexOutOfBoundsException("Not enough readable bytes");
    }

    @Override
    public Buffer readUntilDoubleCRLF() throws IOException {
        throw new IndexOutOfBoundsException("Not enough readable bytes");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReadableBytes() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReadableBytes() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        return EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readUntil(final byte b) throws IOException, ByteNotFoundException {
        throw new ByteNotFoundException(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int start, final int stop) {
        if (start != 0 && stop != 0) {
            throw new IndexOutOfBoundsException("This buffer is empty");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int stop) {
        if (stop != 0) {
            throw new IndexOutOfBoundsException("This buffer is empty");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReaderIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markReaderIndex() {
        // left empty intentionally since there is nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetReaderIndex() {
        // left empty intentionally since there is nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException, IOException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException, IOException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readUnsignedByte() throws IndexOutOfBoundsException, IOException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpAsHex() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    /**
     * Really nothing to clone so just return this since this
     * {@link EmptyBuffer} is by definition immutable.
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer clone() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "";
    }

    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        throw new IndexOutOfBoundsException("Not enough readable bytes");
    }

    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException {
        if (bytes.length == 0) {
            return this;
        }

        throw new ByteNotFoundException(bytes[0]);
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
    IllegalArgumentException {
        return -1;
    }

    @Override
    public int indexOf(final byte b) throws IOException, ByteNotFoundException, IllegalArgumentException {
        return -1;
    }

    @Override
    public void setReaderIndex(final int index) {
        // ignored
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
    WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public int getWriterIndex() {
        return -1;
    }

    @Override
    public int getWritableBytes() {
        return 0;
    }

    @Override
    public boolean hasWritableBytes() {
        return false;
    }

    @Override
    public void getBytes(final Buffer dst) {
        // since it is empty, there are no bytes to get
        // so therefore leaving empty.
    }

    @Override
    public void getByes(final byte[] dst) throws IndexOutOfBoundsException {
        // since it is empty, there are no bytes to get
        // so therefore leaving empty.
    }

    @Override
    public void getBytes(final int index, final Buffer dst) throws IndexOutOfBoundsException {
        // since it is empty, there are no bytes to get
        // so therefore leaving empty.
    }

    @Override
    public boolean hasWriteSupport() {
        return false;
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Sorry, this buffer is empty");
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        throw new NumberFormatException("This buffer is empty and therefore cannot be parsed as an integer");
    }

    @Override
    public int parseToInt(final int radix) {
        throw new NumberFormatException("This buffer is empty and therefore cannot be parsed as an integer");
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This buffer is empty");
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        if (this == other) {
            return true;
        }

        try {
            return ((Buffer) other).isEmpty();
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }
    }

}
