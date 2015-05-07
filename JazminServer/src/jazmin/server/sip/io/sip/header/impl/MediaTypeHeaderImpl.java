/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import java.io.IOException;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.header.MediaTypeHeader;
import jazmin.server.sip.io.sip.impl.SipParser;


/**
 * Base class for {@link MediaTypeHeader}s
 * 
 * @author jonas@jonasborjesson.com
 */
public abstract class MediaTypeHeaderImpl extends ParametersImpl implements MediaTypeHeader {

    private final Buffer mType;

    private final Buffer subType;

    /**
     * @param name
     * @param params
     */
    public MediaTypeHeaderImpl(final Buffer name, final Buffer mType, final Buffer subType, final Buffer params) {
        super(name, params);
        this.mType = mType;
        this.subType = subType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getContentType() {
        return this.mType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getContentSubType() {
        return this.subType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSDP() {
        try {

            if (this.subType.capacity() != 3 || this.mType.capacity() != 11) {
                return false;
            }

            final byte a = this.subType.getByte(0);
            final byte b = this.subType.getByte(1);
            final byte c = this.subType.getByte(2);

            // check for sdp
            if (!((a == 's' || a == 'S') && (b == 'd' || b == 'D') && (c == 'p' || c == 'P'))) {
                return false;
            }

            final byte d = this.mType.getByte(0);
            final byte e = this.mType.getByte(1);
            final byte f = this.mType.getByte(2);
            final byte g = this.mType.getByte(3);
            final byte h = this.mType.getByte(4);
            final byte i = this.mType.getByte(5);
            final byte j = this.mType.getByte(6);
            final byte k = this.mType.getByte(7);
            final byte l = this.mType.getByte(8);
            final byte m = this.mType.getByte(9);
            final byte n = this.mType.getByte(10);

            // check for application
            return (d == 'a' || d == 'A') && (e == 'p' || e == 'P') && (f == 'p' || f == 'P')
                    && (g == 'l' || g == 'L') && (h == 'i' || h == 'I') && (i == 'c' || i == 'C')
                    && (j == 'a' || j == 'A') && (k == 't' || k == 'T') && (l == 'i' || l == 'I')
                    && (m == 'o' || m == 'O') && (n == 'n' || n == 'N');

        } catch (final IOException e) {
            // should really never happen because at this point
            // the Buffer is no longer backed by a stream but
            // rather a buffer array.
            throw new RuntimeException("Strange, IOException when accessing the subtype", e);
        }

    }

    /**
     * 
     */
    @Override
    public Buffer getValue() {
        // TODO: really need to create composite buffers
        final StringBuilder sb = new StringBuilder();
        sb.append(this.mType.toString());
        sb.append((char) SipParser.SLASH);
        sb.append(this.subType.toString());
        final Buffer params = super.getValue();
        if (params != null && !params.isEmpty()) {
            sb.append(params.toString());
        }
        Buffer b= Buffers.wrap(sb.toString());
        return b;
    }
    
    protected void transferValue(final Buffer dst) {
        final Buffer value = getValue();
        value.getBytes(0, dst);
    }
    @Override
    public MediaTypeHeader ensure() {
        return this;
    }

}
