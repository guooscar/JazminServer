/**
 * 
 */
package jazmin.server.sip.io.buffer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;

/**
 * A buffer directly backed by a byte-array
 * 
 * @author jonas@jonasborjesson.com
 */
public final class ByteBuffer extends AbstractBuffer {

    /**
     * The actual buffer
     */
    protected final byte[] buffer;

    /**
     * 
     */
    protected ByteBuffer(final byte[] buffer) {
        this(0, 0, buffer.length, buffer);
    }

    protected ByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary, final byte[] buffer) {
        this(readerIndex, lowerBoundary, upperBoundary, upperBoundary, buffer);
    }

    protected ByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
            final int writerIndex, final byte[] buffer) {
        super(readerIndex, lowerBoundary, upperBoundary, writerIndex);
        assert buffer != null;
        this.buffer = buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int start, final int stop) {
        if (start == stop) {
            return Buffers.EMPTY_BUFFER;
        }
        checkIndex(this.lowerBoundary + start);
        checkIndex(this.lowerBoundary + stop - 1);
        final int upperBoundary = this.lowerBoundary + stop;
        final int writerIndex = upperBoundary;
        return new ByteBuffer(0, this.lowerBoundary + start, upperBoundary, writerIndex, this.buffer);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        if (length == 0) {
            return Buffers.EMPTY_BUFFER;
        }
        checkReadableBytes(length);
        final int lowerBoundary = this.readerIndex + this.lowerBoundary;
        this.readerIndex += length;
        final int upperBoundary = this.readerIndex + this.lowerBoundary;
        final int writerIndex = upperBoundary;
        return new ByteBuffer(0, lowerBoundary, upperBoundary, writerIndex, this.buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReadableBytes() {
        return getReadableBytes() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return getReadableBytes() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        checkIndex(this.lowerBoundary + index);
        return this.buffer[this.lowerBoundary + index];
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        checkWriterIndex(this.writerIndex);
        this.buffer[this.lowerBoundary + this.writerIndex] = b;
        ++this.writerIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        final int length = getReadableBytes();
        final byte[] array = new byte[length];
        System.arraycopy(this.buffer, this.lowerBoundary + this.readerIndex, array, 0, length);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        return getByte(this.readerIndex++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        return getByte(this.readerIndex);
    }

    public long unsignedInt(final byte a, final byte b, final byte c, final byte d) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (c & 0xff) << 8 | d & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        return getInt(this.readerIndex) & 0xFFFFFFFFL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        final int value = getInt(this.readerIndex);
        this.readerIndex += 4;
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        final short value = getShort(this.readerIndex);
        this.readerIndex += 2;
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(final int index) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 3);
        return (this.buffer[i] & 0xff) << 24 | (this.buffer[i + 1] & 0xff) << 16
                | (this.buffer[i + 2] & 0xff) << 8 | (this.buffer[i + 3] & 0xff) << 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);

        // big endian
        return (short) (this.buffer[i] << 8 | this.buffer[i + 1] & 0xFF);

        // little endian
        // return (short) (this.buffer[i] & 0xFF | this.buffer[i + 1] << 8);
    }

    @Override
    public void setUnsignedShort(final int index, final int value) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);
        this.buffer[i] = (byte) (value >> 8);
        this.buffer[i + 1] = (byte) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return getShort(index) & 0xFFFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return (short) (getByte(index) & 0xFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpAsHex() {
        return "dumpAsHex isn't implemented just yet";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer clone() {
        final int size = capacity();
        final byte[] copy = new byte[size];
        System.arraycopy(this.buffer, this.lowerBoundary, copy, 0, size);
        return new ByteBuffer(copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = this.lowerBoundary + this.readerIndex; i < this.upperBoundary; ++i) {
            result = 31 * result + this.buffer[i];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return internalEquals(false, other);
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        return internalEquals(true, other);
    }

    private boolean internalEquals(final boolean ignoreCase, final Object other) {
        try {
            if (this == other) {
                return true;
            }
            final ByteBuffer b = (ByteBuffer) other;
            if (getReadableBytes() != b.getReadableBytes()) {
                return false;
            }


            final int length = getReadableBytes();
            for (int i = 0; i < length; ++i) {
                final byte a1 = this.buffer[this.lowerBoundary + i];
                final byte b1 = b.buffer[b.lowerBoundary + i];
                if (a1 != b1) {
                    if (ignoreCase) {
                        // lazy! Fix this and also this won't really work
                        // for UTF-8 I believe. Def not for UTF-16 but good
                        // enough for now.
                        final String s1 = new String(new byte[] {a1});
                        final String s2 = new String(new byte[] {b1});
                        if (s1.equalsIgnoreCase(s2)) {
                            continue;
                        }
                    }
                    return false;
                }
            }

            return true;
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        this.buffer[i] = value;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        this.buffer[this.lowerBoundary + index] = (byte) value;
    }

    @Override
    public String toString() {
        try {
            return new String(getArray(), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getWriterIndex() {
        return this.writerIndex;
    }

    @Override
    public void getBytes(final Buffer dst) {
        getBytes(getReaderIndex(), dst);
    }

    @Override
    public void getBytes(final int index, final Buffer dst) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index less than zero");
        }
        final int max = dst.getWritableBytes();
        final int stop = Math.min(this.lowerBoundary + index + max, this.writerIndex);
        for (int i = this.lowerBoundary + index; i < stop; ++i) {
            dst.write(this.buffer[i]);
        }
    }

    @Override
    public void getByes(final byte[] dst) throws IndexOutOfBoundsException {
        final int length = Math.min(dst.length, getReadableBytes());
        System.arraycopy(this.buffer, this.lowerBoundary + this.readerIndex, dst, 0, length);
    }

    public void getBytes(final int index, final java.nio.ByteBuffer dst) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index less than zero");
        }
        final int stop = this.lowerBoundary + index;
        // for (int i = this.lowerBoundary + index; i < stop; ++i) {
        for (int i = stop - 1; i >= this.lowerBoundary + index; --i) {
            dst.put(this.buffer[i]);
        }
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        out.write(this.buffer, this.lowerBoundary, this.writerIndex - this.lowerBoundary);
    }

    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean hasWriteSupport() {
        return true;
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
    UnsupportedEncodingException {
        write(s, "UTF-8");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
    WriteNotSupportedException, UnsupportedEncodingException {
        final byte[] bytes = s.getBytes(charset);
        if (!checkWritableBytesSafe(bytes.length)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }

        System.arraycopy(bytes, 0, this.buffer, this.writerIndex, bytes.length);
        this.writerIndex += bytes.length;
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        checkIndex(index);
        checkIndex(index + 3);
        this.buffer[this.lowerBoundary + index + 0] = (byte) (value >>> 24);
        this.buffer[this.lowerBoundary + index + 1] = (byte) (value >>> 16);
        this.buffer[this.lowerBoundary + index + 2] = (byte) (value >>> 8);
        this.buffer[this.lowerBoundary + index + 3] = (byte) value;
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        // setInt(index, (int) value);
        checkIndex(index);
        checkIndex(index + 3);
        this.buffer[this.lowerBoundary + index + 0] = (byte) value;
        this.buffer[this.lowerBoundary + index + 1] = (byte) (value >>> 8);
        this.buffer[this.lowerBoundary + index + 2] = (byte) (value >>> 16);
        this.buffer[this.lowerBoundary + index + 3] = (byte) (value >>> 24);

    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        if (!checkWritableBytesSafe(4)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }
        final int index = this.lowerBoundary + this.writerIndex;
        this.buffer[index + 0] = (byte) (value >>> 24);
        this.buffer[index + 1] = (byte) (value >>> 16);
        this.buffer[index + 2] = (byte) (value >>> 8);
        this.buffer[index + 3] = (byte) value;
        this.writerIndex += 4;
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        if (!checkWritableBytesSafe(8)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }
        final int index = this.lowerBoundary + this.writerIndex;
        this.buffer[index + 0] = (byte) (value >>> 56);
        this.buffer[index + 1] = (byte) (value >>> 48);
        this.buffer[index + 2] = (byte) (value >>> 40);
        this.buffer[index + 3] = (byte) (value >>> 32);
        this.buffer[index + 4] = (byte) (value >>> 24);
        this.buffer[index + 5] = (byte) (value >>> 16);
        this.buffer[index + 6] = (byte) (value >>> 8);
        this.buffer[index + 7] = (byte) value;
        this.writerIndex += 8;
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, this.lowerBoundary + this.writerIndex + size, this.buffer);
        this.writerIndex += size;
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, this.lowerBoundary + this.writerIndex + size, this.buffer);
        this.writerIndex += size;
    }
}
