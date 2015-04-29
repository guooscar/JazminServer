package jazmin.server.sip.io.sip.header.impl;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertArgument;
import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.header.ExpiresHeader;

public class ExpiresHeaderImpl extends SipHeaderImpl implements ExpiresHeader {

    private int expires;

    public ExpiresHeaderImpl(final int expires) {
        super(ExpiresHeader.NAME, null);
        this.expires = expires;
    }

    @Override
    public int getExpires() {
        return this.expires;
    }

    @Override
    public void setExpires(final int expires) {
        assertArgument(expires >= 0, "The value must be greater or equal to zero");
        this.expires = expires;
    }

    @Override
    public Buffer getValue() {
        return Buffers.wrap(this.expires);
    }

    @Override
    public ExpiresHeader clone() {
        return new ExpiresHeaderImpl(this.expires);
    }

    @Override
    public ExpiresHeader ensure() {
        return this;
    }
}
