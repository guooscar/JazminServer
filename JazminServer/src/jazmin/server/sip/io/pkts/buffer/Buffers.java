/**
 * 
 */
package jazmin.server.sip.io.pkts.buffer;

import java.io.InputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public final class Buffers {

    private final static byte[] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4',
        '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6',
        '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8',
        '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', };

    private final static byte[] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
        '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2',
        '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

    /**
     * All possible chars for representing a number as a String
     */
    private final static byte[] digits = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
        'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final static int[] sizeTable = {
        9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    /**
     * An empty buffer.
     */
    public static Buffer EMPTY_BUFFER = new EmptyBuffer();

    /**
     * 
     */
    private Buffers() {
        // left empty intentionally
    }

    /**
     * Converts the integer value into a string and that is what is being
     * wrapped in a {@link Buffer}
     * 
     * @param value
     * @return
     */
    public static Buffer wrap(final int value) {
        final int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
        final byte[] bytes = new byte[size];
        getBytes(value, size, bytes);
        return new ByteBuffer(bytes);
    }

    public static Buffer wrap(final long value) {
        final int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
        final byte[] bytes = new byte[size];
        getBytes(value, size, bytes);
        return new ByteBuffer(bytes);
    }

    public static Buffer wrap(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }

        if (s.isEmpty()) {
            return Buffers.EMPTY_BUFFER;
        }

        return Buffers.wrap(s.getBytes());
    }

    public static Buffer wrap(final InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("the input stream cannot be null or empty");
        }

        return new InputStreamBuffer(is);
    }

    /**
     * Create a new Buffer
     * 
     * @param capacity
     * @return
     */
    public static Buffer createBuffer(final int capacity) {
        final byte[] buffer = new byte[capacity];
        return new ByteBuffer(0, 0, buffer.length, 0, buffer);
    }

    /**
     * Wrap the supplied byte array
     * 
     * @param buffer
     * @return
     */
    public static Buffer wrap(final byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("the buffer cannot be null or empty");
        }

        return new ByteBuffer(buffer);
    }

    /**
     * Same as {@link #wrap(byte[])} but we will clone the byte array first.
     * 
     * @param buffer
     * @return
     */
    public static Buffer wrapAndClone(final byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("the buffer cannot be null or empty");
        }

        final byte[] b = new byte[buffer.length];
        System.arraycopy(buffer, 0, b, 0, buffer.length);
        return new ByteBuffer(b);
    }

    /**
     * Combine two buffers into one. The resulting buffer will share the
     * underlying byte storage so changing the value in one will affect the
     * other. However, the original two buffers will still have their own reader
     * and writer index.
     * 
     * @param one
     * @param two
     * @return
     */
    public static Buffer wrap(final Buffer one, final Buffer two) {
        // TODO: create an actual composite buffer. 
        final int size1 = one != null ? one.getReadableBytes() : 0;
        final int size2 = two != null ? two.getReadableBytes() : 0;
        if (size1 == 0 && size2 > 0) {
            return two.slice();
        } else if (size2 == 0 && size1 > 0) {
            return one.slice();
        } else if (size2 == 0 && size1 == 0) {
            return Buffers.EMPTY_BUFFER;
        }

        final Buffer composite = Buffers.createBuffer(size1 + size2);
        one.getBytes(composite);
        two.getBytes(composite);
        return composite;
    }

    /**
     * Wrap the supplied byte array specifying the allowed range of visible
     * bytes.
     * 
     * @param buffer
     * @param lowerBoundary
     *            the index of the lowest byte that is accessible to this Buffer
     *            (zero based index)
     * @param upperBoundary
     *            the upper boundary (exclusive) of the range of visible bytes.
     * @return
     */
    public static Buffer wrap(final byte[] buffer, final int lowerBoundary, final int upperBoundary) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("the buffer cannot be null or empty");
        }
        if (upperBoundary > buffer.length) {
            throw new IllegalArgumentException("The upper boundary cannot exceed the length of the buffer");
        }
        if (lowerBoundary >= upperBoundary) {
            throw new IllegalArgumentException("The lower boundary must be lower than the upper boundary");
        }

        if (lowerBoundary < 0) {
            throw new IllegalArgumentException("The lower boundary must be a equal or greater than zero");
        }

        final int readerIndex = 0;
        final int writerIndex = upperBoundary;
        return new ByteBuffer(readerIndex, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    /**
     * Copied straight from the Integer class but modified to return bytes instead.
     * 
     * Places characters representing the integer i into the character array buf. The characters are
     * placed into the buffer backwards starting with the least significant digit at the specified
     * index (exclusive), and working backwards from there.
     * 
     * Will fail if i == Integer.MIN_VALUE
     */
    protected static void getBytes(int i, final int index, final byte[] buf) {
        int q, r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = i * 52429 >>> 16 + 3;
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    /**
     * Find out how many characters it would take to represent the value as a string.
     * 
     * @param value
     * @return
     */
    public static int stringSizeOf(final int value) {
        return value < 0 ? stringSize(-value) + 1 : stringSize(value);
    }

    // Requires positive x
    protected static int stringSize(final int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    /**
     * Copied straight from the Long class but modified to return bytes instead.
     * 
     * Places characters representing the integer i into the character array buf. The characters are
     * placed into the buffer backwards starting with the least significant digit at the specified
     * index (exclusive), and working backwards from there.
     *
     * Will fail if i == Long.MIN_VALUE
     */
    protected static void getBytes(long i, final int index, final byte[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = i2 * 52429 >>> 16 + 3;
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = (byte) sign;
        }
    }

    /**
     * Find out how many characters it would take to represent the value as a string.
     * 
     * @param value
     * @return
     */
    public static int stringSizeOf(final long value) {
        return value < 0 ? stringSize(-value) + 1 : stringSize(value);
    }

    // Requires positive x
    protected static int stringSize(final long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }


}
